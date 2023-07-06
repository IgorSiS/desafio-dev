package com.desafio.dev.controller.response;

import com.desafio.dev.domain.TransactionFinance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
