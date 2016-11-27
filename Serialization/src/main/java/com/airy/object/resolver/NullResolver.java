package com.airy.object.resolver;

import com.airy.core.NioBuffer;
import com.airy.object.serializer.Serializer;

import java.lang.reflect.Type;

public class NullResolver extends ObjectResolver {

    public NullResolver(Serializer serializer) {
        super(serializer);
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Type referenceType, Object obj) {
        return false;
    }

    @Override
    public Object readObject(NioBuffer buffer, Type referenceType) {
        return null;
    }
}
