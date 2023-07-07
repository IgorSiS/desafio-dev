package com.desafio.dev.controller.response;

import com.desafio.dev.domain.TransactionFinance;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindTransactionResponse {
    private String storeName;
    private List<TransactionFinance> transactions;
    private double accountBalance;

}
