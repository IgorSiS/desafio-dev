package com.desafio.dev.controller;

import com.desafio.dev.domain.TransactionFinance;
import lombok.*;
import org.springframework.data.domain.Page;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFinanceResponse {
    private Double accountBalance;
    private Page<TransactionFinance> trasacations;

}
