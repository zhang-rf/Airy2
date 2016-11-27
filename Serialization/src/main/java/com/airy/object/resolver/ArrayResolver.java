package com.airy.object.resolver;

import com.airy.core.NioBuffer;
import com.airy.object.serializer.Serializer;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

import static com.airy.object.support.InternalUtils.*;
import static com.airy.object.support.Nulls.isNull;

public class ArrayResolver extends ObjectResolver {

    public ArrayResolver(Serializer serializer) {
        super(serializer);
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Type referenceType, Object obj) {
        Class<?> clazz = obj.getClass();
        if (clazz.isArray()) {
            Class<?> componentType = getComponentType(clazz);
            writeClassName(buffer, componentType);

            Object array = obj;
            do {
                int length = Array.getLength(array);
                buffer.putUnsignedVarint(length);

                for (int i = 0; i < length; i++) {
                    array = Array.get(array, i);
                    if (array != null)
                        break;
                }
            } while (array != null && array.getClass().isArray());
            deepIterate(buffer, obj, 1, componentType);
            buffer.asByteBuffer().put((byte) 0);
            return true;
        }
        return false;
    }

    private int deepIterate(NioBuffer buffer, Object array, int indexer, Class<?> componentType) {
        boolean isArray = array.getClass().getComponentType().isArray();
        boolean isPrimitive = !isArray && isPrimitive(componentType);
        for (int i = 0, length = Array.getLength(array); i < length; i++, indexer++) {
            if (isArray) {
                Object subArray = Array.get(array, i);
                if (subArray != null)
                    indexer = deepIterate(buffer, subArray, indexer, componentType);
            } else {
                Object value = Array.get(array, i);
                if (!isNull(value)) {
                    buffer.putUnsignedVarint(indexer);
                    if (!isPrimitive) {
                        Class<?> clazz = value.getClass();
                        if (clazz != componentType)
                            writeClassName(buffer, clazz);
                        else
                            buffer.putString("");
                    }
                    serializer.serialize(buffer, value, false);
                }
            }
        }
        return indexer - 1;
    }

    @Override
    public Object readObject(NioBuffer buffer, Type referenceType) {
        try {
            if (referenceType == null)
                return null;

            int dimension = 1;
            if (referenceType instanceof Class) {
                Class<?> clazz = (Class<?>) referenceType;
                if (clazz.isArray()) {
                    while ((clazz = clazz.getComponentType()).isArray()) {
                        dimension++;
                    }
                } else
                    return null;
            } else if (referenceType instanceof GenericArrayType) {
                while ((referenceType = ((GenericArrayType) referenceType).getGenericComponentType()) instanceof GenericArrayType) {
                    dimension++;
                }
            } else
                return null;

            Class<?> componentType = readClass(buffer, null);
            int[] dimensions = new int[dimension];
            for (int i = 0; i < dimension; i++)
                dimensions[i] = (int) buffer.getUnsignedVarint();
            Object array = Array.newInstance(componentType, dimensions);
            boolean isPrimitive = isPrimitive(componentType);

            int indexer;
            while ((indexer = (int) buffer.getUnsignedVarint()) != 0) {
                Class<?> clazz = isPrimitive ? componentType : readClass(buffer, componentType);
                setArray(array, dimensions, indexer, serializer.deserialize(buffer, clazz));
            }
            return array;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setArray(Object array, int[] dimensions, int indexer, Object value) {
        int dimension = dimensions.length;
        int[] indexes = new int[dimension];

        for (int i = 0; i < dimension; i++) {
            double factor = 1.0;
            for (int f = dimension - 1; f > i; f--)
                factor *= dimensions[f];
            indexes[i] = (int) Math.ceil(indexer / factor);
            indexer -= (indexes[i] - 1) * factor;
        }

        for (int i = 0; i < dimension - 1; i++)
            array = Array.get(array, indexes[i] - 1);
        Array.set(array, indexes[dimension - 1] - 1, value);
    }
}
