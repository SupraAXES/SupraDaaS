package com.supra.daas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.supra.daas.model.TaskModel;

@Repository
public interface TaskRepository extends JpaRepository<TaskModel, Integer> {
    
}
