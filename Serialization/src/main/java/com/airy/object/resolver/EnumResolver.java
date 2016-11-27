package com.airy.object.resolver;

import com.airy.core.NioBuffer;
import com.airy.object.serializer.Serializer;

import java.lang.reflect.Type;

public class EnumResolver extends ObjectResolver {

    public EnumResolver(Serializer serializer) {
        super(serializer);
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Type referenceType, Object obj) {
        if (obj.getClass().isEnum()) {
            buffer.putUnsignedVarint(((Enum) obj).ordinal());
            return true;
        }
        return false;
    }

    @Override
    public Object readObject(NioBuffer buffer, Type referenceType) {
        if (referenceType != null && referenceType instanceof Class) {
            Class<?> clazz = (Class<?>) referenceType;
            if (clazz.isEnum())
                return clazz.getEnumConstants()[(int) buffer.getUnsignedVarint()];
        }
        return null;
    }
}
