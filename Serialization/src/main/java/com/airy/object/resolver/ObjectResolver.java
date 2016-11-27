package com.airy.object.resolver;

import com.airy.core.NioBuffer;
import com.airy.object.serializer.Serializer;

import java.lang.reflect.Type;

public abstract class ObjectResolver {

    protected Serializer serializer;

    protected ObjectResolver(Serializer serializer) {
        this.serializer = serializer;
    }

    public abstract boolean writeObject(NioBuffer buffer, Type referenceType, Object obj);

    public abstract Object readObject(NioBuffer buffer, Type referenceType);
}
