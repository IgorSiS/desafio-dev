package com.desafio.dev.gateway;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.gateway.exception.GatewayException;

import java.util.List;

public interface FindTransactionByNameGateway {
    List<TransactionFinance> execute(String name) throws GatewayException;
}
