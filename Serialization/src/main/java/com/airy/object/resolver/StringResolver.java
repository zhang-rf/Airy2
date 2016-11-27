package com.airy.object.resolver;

import com.airy.core.NioBuffer;
import com.airy.object.serializer.Serializer;

import java.lang.reflect.Type;

public class StringResolver extends ObjectResolver {

    private String charsetName;

    public StringResolver(Serializer serializer) {
        super(serializer);
        charsetName = "UTF-8";
    }

    public StringResolver(Serializer serializer, String charsetName) {
        super(serializer);
        this.charsetName = charsetName;
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Type referenceType, Object obj) {
        if (obj.getClass() == String.class) {
            buffer.putString((String) obj, charsetName);
            return true;
        }
        return false;
    }

    @Override
    public Object readObject(NioBuffer buffer, Type referenceType) {
        if (referenceType == String.class)
            return buffer.getString(charsetName);
        return null;
    }
}
