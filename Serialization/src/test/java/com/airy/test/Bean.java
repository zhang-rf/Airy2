package com.airy.test;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class Bean extends SuperBean implements Serializable {

    private short var4;
    private int var5;
    private long var6;
    private EmbeddedBean embeddedBean;
    private IBean iBean;
    private int[] array;
    private byte[] bytes;
    private List<?> list;
    private Map<?, ?> map;
    private Bean referenceTest;
}
