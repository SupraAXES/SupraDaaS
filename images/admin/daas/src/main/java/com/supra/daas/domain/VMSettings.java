package com.supra.daas.domain;

import java.util.List;

import lombok.Data;

@Data
public class VMSettings {
    
    private List<VMSettingsTemplate> templates;

    private List<String> cpuSizes;

    private List<String> memorySizes;

    private List<String> diskSizes;

}
