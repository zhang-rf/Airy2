package com.airy.object.serializer;

import com.airy.core.NioBuffer;

public interface Serializer {

    void serialize(NioBuffer buffer, Object obj, boolean writeClassName);

    <T> T deserialize(NioBuffer buffer, Class<T> clazz);
}
