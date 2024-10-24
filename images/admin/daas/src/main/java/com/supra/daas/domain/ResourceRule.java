package com.supra.daas.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ResourceRule {
    
    private Integer keyboard;

    private Integer mouse;

    private Integer audioOut;

    private Integer audioIn;

    /** file **/
    private Integer download;

    private Integer upload;

    private Integer view;

    private Integer edit;

    private Integer copy;

    private Integer paste;

    private Integer idleTime; // idle time minute

}
