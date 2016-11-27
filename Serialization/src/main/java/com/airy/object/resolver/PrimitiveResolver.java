package com.airy.object.resolver;

import com.airy.core.NioBuffer;
import com.airy.object.serializer.Serializer;

import java.lang.reflect.Type;

public class PrimitiveResolver extends ObjectResolver {

    public PrimitiveResolver(Serializer serializer) {
        super(serializer);
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Type referenceType, Object obj) {
        if (obj instanceof Boolean)
            buffer.asByteBuffer().put((byte) ((boolean) obj ? 1 : 0));
        else if (obj instanceof Character)
            buffer.putVarint((char) obj);
        else if (obj instanceof Byte)
            buffer.putVarint((byte) obj);
        else if (obj instanceof Short)
            buffer.putVarint((short) obj);
        else if (obj instanceof Integer)
            buffer.putVarint((int) obj);
        else if (obj instanceof Long)
            buffer.putVarint((long) obj);
        else if (obj instanceof Float)
            buffer.putFloat((float) obj);
        else if (obj instanceof Double)
            buffer.putDouble((double) obj);
        else
            return false;
        return true;
    }

    @Override
    public Object readObject(NioBuffer buffer, Type referenceType) {
        if (referenceType == boolean.class || referenceType == Boolean.class)
            return buffer.asByteBuffer().get() == 1;
        else if (referenceType == char.class || referenceType == Character.class)
            return (char) buffer.getVarint();
        else if (referenceType == byte.class || referenceType == Byte.class)
            return (byte) buffer.getVarint();
        else if (referenceType == short.class || referenceType == Short.class)
            return (short) buffer.getVarint();
        else if (referenceType == int.class || referenceType == Integer.class)
            return (int) buffer.getVarint();
        else if (referenceType == long.class || referenceType == Long.class)
            return buffer.getVarint();
        else if (referenceType == float.class || referenceType == Float.class)
            return buffer.getFloat();
        else if (referenceType == double.class || referenceType == Double.class)
            return buffer.getDouble();
        else
            return null;
    }
}
