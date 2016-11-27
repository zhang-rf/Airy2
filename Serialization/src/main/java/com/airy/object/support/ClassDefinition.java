package com.airy.object.support;

import java.util.*;
import java.util.concurrent.*;

public class ClassDefinition {

    private static final int INDEX_OFFSET = 8192;

    private static List<Class<?>> classList = new ArrayList<>();
    private static Map<Class<?>, Integer> classMap = new HashMap<>();

    static {
        classList.add(boolean.class);
        classList.add(char.class);
        classList.add(byte.class);
        classList.add(short.class);
        classList.add(int.class);
        classList.add(long.class);
        classList.add(float.class);
        classList.add(double.class);
        classList.add(Boolean.class);
        classList.add(Character.class);
        classList.add(Byte.class);
        classList.add(Short.class);
        classList.add(Integer.class);
        classList.add(Long.class);
        classList.add(Float.class);
        classList.add(Double.class);
        classList.add(String.class);

        classList.add(ArrayList.class);
        classList.add(LinkedList.class);

        classList.add(PriorityQueue.class);

        classList.add(HashMap.class);
        classList.add(EnumMap.class);
        classList.add(IdentityHashMap.class);
        classList.add(LinkedHashMap.class);
        classList.add(TreeMap.class);
        classList.add(WeakHashMap.class);

        classList.add(HashSet.class);
        classList.add(BitSet.class);
        classList.add(LinkedHashSet.class);
        classList.add(TreeSet.class);

        classList.add(CopyOnWriteArrayList.class);

        classList.add(ArrayBlockingQueue.class);
        classList.add(ConcurrentLinkedQueue.class);
        classList.add(DelayQueue.class);
        classList.add(LinkedBlockingQueue.class);
        classList.add(PriorityBlockingQueue.class);
        classList.add(SynchronousQueue.class);

        classList.add(ConcurrentHashMap.class);

        classList.add(CopyOnWriteArraySet.class);

        for (int i = 0, size = classList.size(); i < size; i++)
            classMap.put(classList.get(i), i);
    }

    private ClassDefinition() {
    }

    public static boolean contains(int index) {
        return index >= INDEX_OFFSET;
    }

    public static Class<?> getClass(int index) {
        return index < INDEX_OFFSET ? null : classList.get(index - INDEX_OFFSET);
    }

    public static int getIndex(Class<?> clazz) {
        Integer index = classMap.get(clazz);
        return index != null ? index + INDEX_OFFSET : -1;
    }
}
