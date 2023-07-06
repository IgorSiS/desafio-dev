package com.desafio.dev.gateway;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.gateway.exception.GatewayException;

import java.util.List;

public interface CreateTransactionsGateway {
    void execute(List<TransactionFinance> transactionFinances) throws GatewayException;
}
