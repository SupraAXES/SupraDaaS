package com.supra.daas.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AccountInfo {
    
    private String user;

    private String pass;

    private String pri;

    private Boolean def;
}
