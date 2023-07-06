package com.desafio.dev.domain;

import com.desafio.dev.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFinance {

    private TransactionType type;
    private LocalDate date;
    private double value;
    private String cpf;
    private String card;
    private LocalTime time;
    private String storeOwner;
    private String storeName;
}
