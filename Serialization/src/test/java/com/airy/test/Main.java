package com.airy.test;

import com.airy.object.ObjectMap;
import com.airy.object.SerializationHelper;
import com.airy.object.serializer.StructuredSerializer;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Bean bean = new Bean();
        bean.setVar1(false);
        bean.setVar2((byte) 1);
        bean.setVar3('C');

        bean.setVar4((short) -3);
        bean.setVar5(4);
        bean.setVar6(-5L);

        EmbeddedBean embeddedBean = new EmbeddedBean();
        embeddedBean.setVar7(0.1F);
        embeddedBean.setVar8(1.1);
        embeddedBean.setVar9("Hello World!");
        bean.setEmbeddedBean(embeddedBean);

        EmbeddedBean iBean = new EmbeddedBean();
        iBean.setVar7(11.1F);
        iBean.setVar8(-11.1F);
        iBean.setVar9("END");
        bean.setIBean(iBean);

        bean.setArray(new int[]{3, 0, 1});
//        bean.setBytes(new byte[4 * 1024 * 1024]); //big data test

        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        bean.setList(list);

        Map<String, List<String>> map = new HashMap<>();
        map.put("AAA", list);
        map.put("BBB", list);
        map.put("CCC", list);
        bean.setMap(map);

        bean.setReferenceTest(bean);

        long t1 = System.currentTimeMillis();
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArray);
        outputStream.writeObject(bean);
        long t2 = System.currentTimeMillis();
        System.out.println("Java Serializable Size: " + byteArray.size());
        System.out.println("Java Serializable Time: " + (t2 - t1) + "ms");
        System.out.println();

        t1 = System.currentTimeMillis();
        byte[] data = SerializationHelper.getHelper(StructuredSerializer.class).serialize(bean);
        t2 = System.currentTimeMillis();
        System.out.println("Size: " + data.length);
        System.out.println("Serializing Time: " + (t2 - t1) + "ms");
        System.out.println(Arrays.toString(data));
        System.out.println(new String(data));

        t1 = System.currentTimeMillis();
        Bean newBean = (Bean) SerializationHelper.getHelper(StructuredSerializer.class).deserialize(data);
        t2 = System.currentTimeMillis();
        System.out.println("DeSerializing Time: " + (t2 - t1) + "ms");
        System.out.println("Reference Test: " + (newBean.getReferenceTest() == newBean));
        bean.setReferenceTest(null);
        newBean.setReferenceTest(null);
        System.out.println(newBean);
        System.out.println("bean.equals(newBean) == " + bean.equals(newBean));
        System.out.println();

        System.out.println("ObjectMap Test");
        Map<String, Object> objectMap = new ObjectMap<>(data);
        for (Field field : Bean.class.getDeclaredFields()) {
            String fieldName = field.getName();
            if (!fieldName.equals("referenceTest"))
                System.out.println(fieldName + " = " + objectMap.get(fieldName));
            else {
                Bean referenceBean = (Bean) objectMap.get("referenceTest");
                referenceBean.setReferenceTest(null);
                System.out.println("referenceTest = " + referenceBean);
            }
        }
    }
}
