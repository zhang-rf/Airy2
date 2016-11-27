package com.airy.test;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmbeddedBean implements IBean, Serializable {

    private float var7;
    private double var8;
    private String var9;
}
