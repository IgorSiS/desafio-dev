package com.desafio.dev.usecase;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.domain.exception.UseCaseException;
import com.desafio.dev.enums.TransactionType;
import com.desafio.dev.gateway.FindTransactionByNameGateway;
import com.desafio.dev.gateway.exception.GatewayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FindTransactionByNameUseCaseTest {
    @Mock
    private FindTransactionByNameGateway findTransactionByNameGateway;

    @InjectMocks
    private FindTransactionByNameUseCase findTransactionByNameUseCase;

    @Test
    public void shouldFindTransactionsByNameWhenStoreExists() throws GatewayException {

        List<TransactionFinance> transactions = Collections.singletonList(
                TransactionFinance.builder()
                        .card("123456789012")
                        .cpf("12345678900")
                        .date(LocalDate.now())
                        .storeName("loja")
                        .storeOwner("Owner Test")
                        .time(LocalTime.now())
                        .type(TransactionType.DEBIT)
                        .value(100.0)
                        .build());

        when(findTransactionByNameGateway.execute(eq("loja")))
                .thenReturn(transactions);

        List<TransactionFinance> result = findTransactionByNameUseCase.execute("loja");

        assertNotNull(result);
    }

    @Test
    void shouldReturnEmptyListWhenStoreHasNoTransactions() throws GatewayException {

        when(findTransactionByNameGateway.execute(eq("Store Test")))
                .thenReturn(new ArrayList<>());

        List<TransactionFinance> result = findTransactionByNameUseCase.execute("Store Test");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyPageWhenStoreDoesNotExist() throws UseCaseException, GatewayException {
        when(findTransactionByNameGateway.execute(eq("No existent Store")))
                .thenReturn(new ArrayList<>());

        List<TransactionFinance> result = findTransactionByNameUseCase.execute("No existent Store");

        assertTrue(result.isEmpty());
    }

}