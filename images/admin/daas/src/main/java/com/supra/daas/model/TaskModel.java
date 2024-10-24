package com.supra.daas.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "ra_task")
public class TaskModel extends BaseModel {
    
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "category", nullable = false)
    private String category;
 
    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "info", nullable = false)
    private String info;

    @Column(name = "status", nullable = false)
    private Integer status;

}
