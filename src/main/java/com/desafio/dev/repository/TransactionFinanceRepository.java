package com.desafio.dev.repository;

import com.desafio.dev.gateway.database.model.TransacationFinanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionFinanceRepository extends JpaRepository<TransacationFinanceEntity, String> {
    List<TransacationFinanceEntity> findAllByStoreName(String nameStore);
}
