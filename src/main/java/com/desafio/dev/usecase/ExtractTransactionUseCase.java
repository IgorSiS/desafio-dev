package com.desafio.dev.usecase;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.domain.exception.UseCaseException;
import com.desafio.dev.enums.TransactionType;
import com.desafio.dev.CreateTransactionsGateway;
import com.desafio.dev.exception.GatewayException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractTransactionUseCase {

    private final CreateTransactionsGateway createTransactionsGateway;

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

    public List<TransactionFinance> execute(InputStream inputStream) throws IOException, UseCaseException {

        try{
            List<TransactionFinance> transactionFinances = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    TransactionFinance transactionFinance = TransactionFinance.builder()
                            .type(TransactionType.getByCode(Integer.parseInt(line.substring(0, 1))))
                            .date(LocalDate.parse(line.substring(1, 9), dateFormatter))
                            .value(Double.parseDouble(line.substring(9, 19)) / 100.00)
                            .cpf(line.substring(19, 30))
                            .card(line.substring(30, 42))
                            .time(LocalTime.parse(line.substring(42, 48), timeFormatter))
                            .storeOwner(line.substring(48, 62).trim())
                            .storeName(line.substring(62).trim())
                            .build();
                    transactionFinances.add(transactionFinance);
                }
            }
            return this.createTransactionsGateway.execute(transactionFinances);
        }catch (GatewayException ex){
            throw new UseCaseException("Problemas ao criar transações");
        }
    }
}