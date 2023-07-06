package com.desafio.dev;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.exception.GatewayException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindTransactionByNameGateway {
    Page<TransactionFinance> execute(String name, Pageable pageable) throws GatewayException;
}
