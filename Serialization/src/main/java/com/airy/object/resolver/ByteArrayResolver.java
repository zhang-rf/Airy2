package com.airy.object.resolver;

import com.airy.core.NioBuffer;
import com.airy.object.serializer.Serializer;

import java.lang.reflect.Type;

public class ByteArrayResolver extends ObjectResolver {

    public ByteArrayResolver(Serializer serializer) {
        super(serializer);
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Type referenceType, Object obj) {
        if (obj.getClass() == byte[].class) {
            byte[] bytes = (byte[]) obj;
            buffer.putUnsignedVarint(bytes.length);
            buffer.asByteBuffer().put(bytes);
            return true;
        }
        return false;
    }

    @Override
    public Object readObject(NioBuffer buffer, Type referenceType) {
        if (referenceType == byte[].class) {
            byte[] bytes = new byte[(int) buffer.getUnsignedVarint()];
            buffer.asByteBuffer().get(bytes);
            return bytes;
        }
        return null;
    }
}
