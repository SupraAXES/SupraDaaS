package com.supra.daas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.supra.daas.model.ResourceModel;

@Repository
public interface ResourceRepository extends JpaRepository<ResourceModel, String> {
    
}
