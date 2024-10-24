package com.supra.daas.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "ra_session")
public class SessionModel extends BaseModel {
    
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(name = "info", nullable = false)
    private String info;

}
