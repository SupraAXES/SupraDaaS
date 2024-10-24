package com.supra.daas.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "ra_resource")
public class ResourceModel extends BaseModel {
    
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "icon", nullable = false)
    private String icon;

    @Column(name = "info", nullable = false)
    private String info;

}
