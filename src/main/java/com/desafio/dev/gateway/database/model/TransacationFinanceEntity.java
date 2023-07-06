package com.desafio.dev.gateway.database.model;

import com.desafio.dev.enums.TransactionType;
import jakarta.persistence.*;
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
@Entity
@Table(name = "transaction_finance")
public class TransacationFinanceEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "type")
    @Enumerated(EnumType.ORDINAL)
    private TransactionType type;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "value")
    private double value;

    @Column(name = "cpf")
    private String cpf;

    @Column(name = "card")
    private String card;

    @Column(name = "time")
    private LocalTime time;

    @Column(name = "storeOwner")
    private String storeOwner;

    @Column(name = "storeName")
    private String storeName;
}
