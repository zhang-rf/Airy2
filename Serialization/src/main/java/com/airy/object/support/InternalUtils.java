package com.airy.object.support;

import com.airy.core.NioBuffer;

import java.lang.reflect.Modifier;

public class InternalUtils {

    private InternalUtils() {
    }

    public static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive() || type.isEnum() || type == String.class ||
                type == Boolean.class || type == Character.class || Number.class.isAssignableFrom(type);
    }

    public static Class<?> getComponentType(Class<?> type) {
        while (type.isArray()) {
            type = type.getComponentType();
        }
        return type;
    }

    public static boolean isMemberField(int modifiers) {
        return !(Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers));
    }

    public static void writeClassName(NioBuffer buffer, Class<?> clazz) {
        int classIndex = ClassDefinition.getIndex(clazz);
        if (classIndex > 0)
            buffer.putUnsignedVarint(classIndex);
        else
            buffer.putString(clazz.getName());
    }

    public static Class<?> readClass(NioBuffer buffer, Class<?> defaultClass) {
        try {
            int classIndex = (int) buffer.getUnsignedVarint();
            if (ClassDefinition.contains(classIndex))
                return ClassDefinition.getClass(classIndex);
            else {
                String className = buffer.revert(NioBuffer.sizeofVarint(classIndex)).getString();
                if (!"".equals(className))
                    return Class.forName(className);
            }
            return defaultClass;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
