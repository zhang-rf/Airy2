package com.airy.object.serializer;

import com.airy.core.NioBuffer;
import com.airy.object.UnknownClassException;
import com.airy.object.resolver.*;
import com.airy.object.support.ClassDefinition;
import com.airy.util.ThreadLocalReference;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static com.airy.object.support.InternalUtils.isMemberField;
import static com.airy.object.support.InternalUtils.readClass;
import static com.airy.object.support.Nulls.isNull;

public class StructuredSerializer implements Serializer {

    private static final Object PRESENT = new Object();
    protected List<ObjectResolver> resolvers = new ArrayList<>();
    protected ObjectResolver masterResolver = new Resolver(this);
    private ThreadLocalReference<Map<Object, Integer>> objectMapReference = new ThreadLocalReference<Map<Object, Integer>>() {

        @Override
        protected Reference<Map<Object, Integer>> initialValue() {
            return new SoftReference<>(new IdentityHashMap<>());
        }
    };
    private ThreadLocalReference<Map<Integer, Object>> addressMapReference = new ThreadLocalReference<Map<Integer, Object>>() {

        @Override
        protected Reference<Map<Integer, Object>> initialValue() {
            return new SoftReference<>(new IdentityHashMap<>());
        }
    };

    public StructuredSerializer() {
        boolean modifiable = addResolvers();
        resolvers.add(new NullResolver(this));
        if (!modifiable)
            resolvers = Collections.unmodifiableList(resolvers);
    }

    protected boolean addResolvers() {
        resolvers.add(new PrimitiveResolver(this));
        resolvers.add(new EnumResolver(this));
        resolvers.add(new StringResolver(this));
        resolvers.add(new ByteArrayResolver(this));
        resolvers.add(new ArrayResolver(this));
        resolvers.add(new CollectionResolver(this));
        resolvers.add(new MapResolver(this));
        return false;
    }

    public StructuredSerializer beforeSerializing() {
        objectMapReference.get().clear();
        return this;
    }

    public StructuredSerializer beforeDeserializing() {
        addressMapReference.get().clear();
        return this;
    }

    @Override
    public void serialize(NioBuffer buffer, Object obj, boolean writeClassName) {
        if (!masterResolver.writeObject(buffer, null, obj))
            serialize0(buffer, obj, writeClassName);
    }

    private void serialize0(NioBuffer buffer, Object obj, boolean writeClassName) {
        try {
            Map<Object, Integer> objectMap = objectMapReference.get();
            objectMap.put(obj, buffer.position());

            buffer.mark().skip(4);
            int baseAddress = buffer.position();

            Class<?> clazz = obj.getClass();
            int classIndex = ClassDefinition.getIndex(clazz);
            String className = clazz.getName();
            Map<Field, Integer> fieldMap = new HashMap<>();
            do {
                for (Field field : clazz.getDeclaredFields()) {
                    if (!isMemberField(field.getModifiers()))
                        continue;

                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (isNull(value, field.getType()))
                        continue;

                    if (objectMap.containsKey(value))
                        fieldMap.put(field, -objectMap.get(value));
                    else {
                        int address = buffer.position();
                        Type referenceType = field.getGenericType();
                        fieldMap.put(field, address);
                        objectMap.put(value, address);
                        if (!masterResolver.writeObject(buffer, referenceType, value))
                            serialize0(buffer, value, value.getClass() != referenceType);
                    }

                }
            } while ((clazz = clazz.getSuperclass()) != Object.class);

            int headerAddress = buffer.position();
            buffer.reset().unmark().asByteBuffer().putInt(headerAddress).position(headerAddress);
            if (!writeClassName)
                buffer.putString("");
            else {
                if (classIndex > 0)
                    buffer.putUnsignedVarint(classIndex);
                else
                    buffer.putString(className);
            }
            buffer.putUnsignedVarint(fieldMap.size());
            for (Map.Entry<Field, Integer> fieldEntry : fieldMap.entrySet()) {
                buffer.asByteBuffer().putShort((short) fieldEntry.getKey().getName().hashCode());
                int address = fieldEntry.getValue();
                buffer.putUnsignedVarint(address > 0 ? address - baseAddress : -address + headerAddress);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(NioBuffer buffer, Class<T> clazz) {
        Object instance = masterResolver.readObject(buffer, clazz);
        if (instance == null)
            instance = deserialize0(buffer, clazz);
        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize0(NioBuffer buffer, Class<T> clazz) {
        try {
            Map<Integer, Object> addressMap = addressMapReference.get();
            if(!addressMap.containsKey(buffer.position()))
            addressMap.put(buffer.position(), PRESENT);

            int headerAddress = buffer.asByteBuffer().getInt();
            int baseAddress = buffer.position();

            buffer.position(headerAddress);
            if ((clazz = (Class) readClass(buffer, clazz)) == null)
                throw new UnknownClassException();
            Constructor<?> defaultConstructor = clazz.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            Object instance = defaultConstructor.newInstance();

            int fieldSize = (int) buffer.getUnsignedVarint();
            Map<Short, Field> hashcodeMap = new HashMap<>(fieldSize);
            do {
                for (Field field : clazz.getDeclaredFields()) {
                    if (isMemberField(field.getModifiers()))
                        hashcodeMap.put((short) field.getName().hashCode(), field);
                }
            } while ((clazz = (Class) clazz.getSuperclass()) != Object.class);

            for (int i = 0; i < fieldSize; i++) {
                Field field = hashcodeMap.get(buffer.asByteBuffer().getShort());
                int offset = (int) buffer.getUnsignedVarint();
                if (field == null)
                    continue;

                field.setAccessible(true);
                int address = (offset < headerAddress) ? (baseAddress + offset) : (offset - headerAddress);
                if (addressMap.containsKey(address)) {
                    Object value = addressMap.get(address);
                    field.set(instance, value == PRESENT ? instance : value);
                } else {
                    buffer.mark().position(address);
                    Type referenceType = field.getGenericType();
                    Object value = masterResolver.readObject(buffer, referenceType);
                    if (value == null) {
                        if (referenceType instanceof Class)
                            value = deserialize0(buffer, (Class<?>) referenceType);
                        else if (referenceType instanceof ParameterizedType)
                            value = deserialize0(buffer, (Class<?>) ((ParameterizedType) referenceType).getRawType());
                        else
                            value = deserialize0(buffer, null);
                    }
                    field.set(instance, value);
                    addressMap.put(address, value);
                    buffer.reset().unmark();
                }
            }
            return (T) instance;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class Resolver extends ObjectResolver {

        Resolver(Serializer serializer) {
            super(serializer);
        }

        @Override
        public boolean writeObject(NioBuffer buffer, Type referenceType, Object obj) {
            for (ObjectResolver resolver : resolvers) {
                if (resolver.writeObject(buffer, referenceType, obj))
                    return true;
            }
            return false;
        }

        @Override
        public Object readObject(NioBuffer buffer, Type referenceType) {
            for (ObjectResolver resolver : resolvers) {
                Object instance = resolver.readObject(buffer, referenceType);
                if (instance != null)
                    return instance;
            }
            return null;
        }
    }
}
