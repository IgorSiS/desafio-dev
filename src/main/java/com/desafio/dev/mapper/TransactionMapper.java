package com.desafio.dev.mapper;

import com.desafio.dev.controller.response.FindTransactionResponse;
import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.gateway.database.model.TransacationFinanceEntity;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionFinance toTransactionFinance(TransacationFinanceEntity entity);

    List<TransacationFinanceEntity> toTransactionFinanceEntityList(List<TransactionFinance> transactionFinances);

    List<TransactionFinance> toTransactionFinanceList(List<TransacationFinanceEntity> savedEntities);

    default List<FindTransactionResponse> toFindTransactionResponse(List<TransactionFinance> transactionFinances) {
        Map<String, List<TransactionFinance>> transactionsByStore = transactionFinances.stream()
                .collect(Collectors.groupingBy(TransactionFinance::getStoreName));

        List<FindTransactionResponse> findTransactionResponses = new ArrayList<>();
        double totalAccountBalance = 0.0;

        for (Map.Entry<String, List<TransactionFinance>> entry : transactionsByStore.entrySet()) {
            String storeName = entry.getKey();
            List<TransactionFinance> transactions = entry.getValue();

            double accountBalance = transactions.stream()
                    .mapToDouble(TransactionFinance::getValue)
                    .sum();

            totalAccountBalance += accountBalance;

            FindTransactionResponse findTransactionResponse = FindTransactionResponse.builder()
                    .storeName(storeName)
                    .transactions(transactions)
                    .accountBalance(accountBalance)
                    .build();

            findTransactionResponses.add(findTransactionResponse);
        }

        return findTransactionResponses;
    }
}
