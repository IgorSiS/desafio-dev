package com.desafio.dev.mapper;

import com.desafio.dev.controller.response.TransactionFinanceResponse;
import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.gateway.database.model.TransacationFinanceEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;


@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionFinance toTransactionFinance(TransacationFinanceEntity entity);

    List<TransacationFinanceEntity> toTransactionFinanceEntityList(List<TransactionFinance> transactionFinances);

    List<TransactionFinance> toTransactionFinanceList(List<TransacationFinanceEntity> savedEntities);

    default TransactionFinanceResponse toTransactionFinanceResponse(Page<TransactionFinance> transactionPage) {
        TransactionFinanceResponse response = new TransactionFinanceResponse();
        response.setAccountBalance(calculateAccountBalance(transactionPage));
        response.setTrasacations(transactionPage);
        return response;
    }

    default Double calculateAccountBalance(Page<TransactionFinance> transactionPage) {
        return transactionPage.stream()
                .mapToDouble(TransactionFinance::getValue)
                .sum();
    }
}
