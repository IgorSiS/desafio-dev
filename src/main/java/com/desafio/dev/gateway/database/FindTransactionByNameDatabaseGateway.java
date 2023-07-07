package com.desafio.dev.gateway.database;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.gateway.FindTransactionByNameGateway;
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
public class FindTransactionByNameDatabaseGateway implements FindTransactionByNameGateway {

    private final TransactionFinanceRepository transactionFinanceRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public List<TransactionFinance> execute(String name) throws GatewayException {
        try {
            List<TransacationFinanceEntity> list;

            if (name == null || name.isEmpty()) {
                list = transactionFinanceRepository.findAll();
            } else {
                list = transactionFinanceRepository.findAllByStoreName(name);
            }
            return transactionMapper.toTransactionFinanceList(list);

        } catch (Exception ex) {
            log.error("Problemas ao consultar transações", ex);
            throw new GatewayException("Problemas ao consultar transações", ex);
        }
    }
}
