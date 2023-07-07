package com.desafio.dev.usecase;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.domain.exception.UseCaseException;
import com.desafio.dev.gateway.FindTransactionByNameGateway;
import com.desafio.dev.gateway.exception.GatewayException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindTransactionByNameUseCase {

    private final FindTransactionByNameGateway findTransactionByNameGateway;

    public List<TransactionFinance> execute(String storeName) throws UseCaseException{
        try{
            return this.findTransactionByNameGateway.execute(storeName);
        }catch (GatewayException ex){
            throw new UseCaseException("Problemas ao consultar transações por nome");
        }
    }
}