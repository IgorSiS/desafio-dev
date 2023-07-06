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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        Page<TransactionFinance> transactionsPage = new PageImpl<>(transactions);

        when(findTransactionByNameGateway.execute(eq("loja"), any(Pageable.class)))
                .thenReturn(transactionsPage);

        Page<TransactionFinance> result = findTransactionByNameUseCase.execute("loja", 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("loja", result.getContent().get(0).getStoreName());
    }

    @Test
    void shouldReturnEmptyListWhenStoreHasNoTransactions() throws GatewayException {

        when(findTransactionByNameGateway.execute(eq("Store Test"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        Page<TransactionFinance> result = findTransactionByNameUseCase.execute("Store Test", 0, 5);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyPageWhenStoreDoesNotExist() throws UseCaseException, GatewayException {
        when(findTransactionByNameGateway.execute(eq("No existent Store"), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<TransactionFinance> result = findTransactionByNameUseCase.execute("No existent Store", 0, 5);

        assertTrue(result.isEmpty());
    }

}