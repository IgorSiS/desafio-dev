package com.desafio.dev.database;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.exception.GatewayException;
import com.desafio.dev.CreateTransactionsGateway;
import com.desafio.dev.database.model.TransacationFinanceEntity;
import com.desafio.dev.repository.TransactionFinanceRepository;
import com.desafio.dev.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateTransactionsDatabaseGateway implements CreateTransactionsGateway {

    private final TransactionFinanceRepository transactionFinanceRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public List<TransactionFinance> execute(List<TransactionFinance> transactionFinances) throws GatewayException {
        try {
            List<TransacationFinanceEntity> transacationFinanceEntities = transactionMapper.toTransactionFinanceEntityList(transactionFinances);
            List<TransacationFinanceEntity> savedEntities = transactionFinanceRepository.saveAll(transacationFinanceEntities);
            return transactionMapper.toTransactionFinanceList(savedEntities);
        } catch (Exception ex) {
            throw new GatewayException("Problemas ao salvar transações", ex);
        }
    }
}
