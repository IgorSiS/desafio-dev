package com.desafio.dev.database;

import com.desafio.dev.FindTransactionByNameGateway;
import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.exception.GatewayException;
import com.desafio.dev.database.model.TransacationFinanceEntity;
import com.desafio.dev.repository.TransactionFinanceRepository;
import com.desafio.dev.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FindTransactionByNameDatabaseGateway implements FindTransactionByNameGateway {

    private final TransactionFinanceRepository transactionFinanceRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public Page<TransactionFinance> execute(String name, Pageable pageable) throws GatewayException {
        try{
            Page<TransacationFinanceEntity> transactionPage =
                    this.transactionFinanceRepository.findAllByStoreName(name, pageable);

            return transactionPage.map(transactionMapper::toTransactionFinance);

        }catch (Exception ex){
            throw new GatewayException("Problemas ao consultar transações",ex);
        }
    }
}
