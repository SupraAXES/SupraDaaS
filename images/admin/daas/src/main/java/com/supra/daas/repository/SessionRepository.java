package com.supra.daas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.supra.daas.model.SessionModel;

@Repository
public interface SessionRepository extends JpaRepository<SessionModel, String> {
    
}
