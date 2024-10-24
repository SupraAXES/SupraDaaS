package com.supra.daas.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class WindowSize {

    private int width;

    private int height;

}
