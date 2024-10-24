package com.supra.daas.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AccessInfo {
    
    private String pl;

    private String port;

    private Boolean def;

}
