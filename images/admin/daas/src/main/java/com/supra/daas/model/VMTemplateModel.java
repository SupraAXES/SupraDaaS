package com.supra.daas.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "ra_vm_template")
public class VMTemplateModel extends BaseModel {
    
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "os_type", nullable = false)
    private String osType;

    @Column(name = "disk_size", nullable = false)
    private String diskSize;

    @Column(name = "system_info", nullable = false)
    private String systemInfo;
}
