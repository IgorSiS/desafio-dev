package com.desafio.dev;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.exception.GatewayException;

import java.util.List;

public interface CreateTransactionsGateway {
    List<TransactionFinance> execute(List<TransactionFinance> transactionFinances) throws GatewayException;
}
