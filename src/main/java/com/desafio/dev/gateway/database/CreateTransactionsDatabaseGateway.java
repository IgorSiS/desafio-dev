package com.desafio.dev.gateway.database;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.gateway.CreateTransactionsGateway;
import com.desafio.dev.gateway.database.model.TransacationFinanceEntity;
import com.desafio.dev.gateway.exception.GatewayException;
import com.desafio.dev.mapper.TransactionMapper;
import com.desafio.dev.repository.TransactionFinanceRepository;
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
    public void execute(List<TransactionFinance> transactionFinances) throws GatewayException {
        try {
            List<TransacationFinanceEntity> transacationFinanceEntities = transactionMapper.toTransactionFinanceEntityList(transactionFinances);
            this.transactionFinanceRepository.saveAll(transacationFinanceEntities);
        } catch (Exception ex) {
            log.error("Problemas ao salvar transações", ex);
            throw new GatewayException("Problemas ao salvar transações", ex);
        }
    }
}
