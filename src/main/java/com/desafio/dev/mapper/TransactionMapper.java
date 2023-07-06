package com.desafio.dev.mapper;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.gateway.database.model.TransacationFinanceEntity;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionFinance toTransactionFinance(TransacationFinanceEntity entity);
}
