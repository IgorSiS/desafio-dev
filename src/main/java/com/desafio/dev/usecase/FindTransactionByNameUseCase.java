package com.desafio.dev.usecase;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.domain.exception.UseCaseException;
import com.desafio.dev.gateway.FindTransactionByNameGateway;
import com.desafio.dev.gateway.exception.GatewayException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindTransactionByNameUseCase {

    private final FindTransactionByNameGateway findTransactionByNameGateway;

    public Page<TransactionFinance> execute(String storeName,
                                            Integer page,
                                            Integer size) throws GatewayException {
        try{
            Pageable pageable = Pageable.ofSize(size).withPage(page);
            return this.findTransactionByNameGateway.execute(storeName,pageable);
        }catch (GatewayException ex){
            throw new UseCaseException("Problemas ao consultar transações por nome");
        }
    }
}