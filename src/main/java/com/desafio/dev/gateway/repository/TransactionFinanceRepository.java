package com.desafio.dev.gateway.repository;

import com.desafio.dev.gateway.database.model.TransacationFinanceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionFinanceRepository extends JpaRepository<TransacationFinanceEntity, String> {
    Page<TransacationFinanceEntity> findAllByStoreName(String nameStore, Pageable pageable);
}
