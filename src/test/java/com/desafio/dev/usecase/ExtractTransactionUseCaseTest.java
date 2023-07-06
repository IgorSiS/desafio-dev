package com.desafio.dev.usecase;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.domain.exception.UseCaseException;
import com.desafio.dev.gateway.CreateTransactionsGateway;
import com.desafio.dev.gateway.exception.GatewayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExtractTransactionUseCaseTest {

    @Mock
    private CreateTransactionsGateway createTransactionsGateway;

    @InjectMocks
    private ExtractTransactionUseCase extractTransactionUseCase;

    @Test
    void shouldProcessFileAndCreateTransactions() throws UseCaseException, GatewayException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "transaction.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "2201903010000000500232702980567677****8778141808JOSÉ COSTA    MERCEARIA 3 IRMÃOS".getBytes()
        );

        List<TransactionFinance> capturedTransactions = new ArrayList<>();

        doAnswer(invocation -> {
            List<TransactionFinance> transactions = invocation.getArgument(0);
            capturedTransactions.addAll(transactions);
            return null;
        }).when(createTransactionsGateway).execute(anyList());

        List<TransactionFinance> transactionFinances = extractTransactionUseCase.execute(multipartFile);

        assertNotNull(transactionFinances);
        verify(createTransactionsGateway, times(1)).execute(anyList());
        assertEquals(1, capturedTransactions.size());
        assertEquals("JOSÉ COSTA", capturedTransactions.get(0).getStoreOwner());
        assertEquals("MERCEARIA 3 IRMÃOS", capturedTransactions.get(0).getStoreName());
    }

    @Test
    void shouldThrowExceptionWhenFileIsEmpty() {
        MultipartFile multipartFile = new MockMultipartFile("file", new byte[0]);

        assertThrows(UseCaseException.class, () -> extractTransactionUseCase.execute(multipartFile));
    }

    @Test
    void shouldThrowExceptionWhenGatewayFails() throws GatewayException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "transaction.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "2201903010000000500232702980567677****8778141808JOSÉ COSTA    MERCEARIA 3 IRMÃOS".getBytes()
        );

        doThrow(GatewayException.class).when(createTransactionsGateway).execute(any(List.class));

        assertThrows(UseCaseException.class, () -> extractTransactionUseCase.execute(multipartFile));
    }
}