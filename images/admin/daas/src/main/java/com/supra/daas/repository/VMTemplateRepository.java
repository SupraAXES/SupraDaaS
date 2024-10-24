package com.supra.daas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.supra.daas.model.VMTemplateModel;

@Repository
public interface VMTemplateRepository extends JpaRepository<VMTemplateModel, String> {
    
}
