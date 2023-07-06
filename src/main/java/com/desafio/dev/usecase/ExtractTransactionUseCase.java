package com.desafio.dev.usecase;

import com.desafio.dev.gateway.CreateTransactionsGateway;
import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.domain.exception.UseCaseException;
import com.desafio.dev.enums.TransactionType;
import com.desafio.dev.gateway.exception.GatewayException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractTransactionUseCase {

    private final CreateTransactionsGateway createTransactionsGateway;

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

    private static final int BATCH_SIZE = 100; // Aqui defino a quantidade de acordo com o tamanho do meu arquivo
    /*
    Fiz uma solução simples de processamento em batch, porém dependendo do tamanho do arquivo e também
    disponibilidade de ferramentas poderia adicionar mensageria, publicando cada lote em uma fila do SQS AWS e ir processando.
    Ou Adicionar um batch, enfim... Existem várias maneiras de resolver este problema de forma escalável e performática.
     */

    //Estou querendo garantir a atomicidade dos dados em caso de falha.
    //Se eu quisesse optar pelo paralelismo perderia um pouco da atomocidade aqui nesse caso.
    @Transactional
    public List<TransactionFinance> execute(InputStream inputStream) throws UseCaseException {
        List<TransactionFinance> transactionFinances = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    transactionFinances.add(processLine(line));
                    if (transactionFinances.size() == BATCH_SIZE) {
                        this.createTransactionsGateway.execute(transactionFinances);
                        transactionFinances.clear();
                    }
                } catch (Exception e) {
                    log.error("Erro processando a linha " + lineNumber + ": " + e.getMessage());
                    throw new UseCaseException("Problemas ao criar transações", e);
                }
            }

            if (!transactionFinances.isEmpty()) {
                this.createTransactionsGateway.execute(transactionFinances);
            }
        } catch (IOException e) {
            throw new UseCaseException("Problemas ao ler o arquivo de transações", e);
        } catch (GatewayException e) {
            throw new UseCaseException("Problemas ao criar transações", e);
        }
        return transactionFinances;
    }

    private TransactionFinance processLine(String line) throws IllegalArgumentException {
        // O arquivo está estruturado e padronizado então podemos tomar esse número como verdade.
        if (line.length() < 63) {
            throw new IllegalArgumentException("Linha mal formatada");
        }

        TransactionType type;
        LocalDate date;
        double value;
        String cpf, card, storeOwner, storeName;
        LocalTime time;

        try {
            type = TransactionType.getByCode(Integer.parseInt(line.substring(0, 1)));
            date = LocalDate.parse(line.substring(1, 9), dateFormatter);
            value = Double.parseDouble(line.substring(9, 19)) / 100.00;
            cpf = line.substring(19, 30);
            card = line.substring(30, 42);
            time = LocalTime.parse(line.substring(42, 48), timeFormatter);
            storeOwner = line.substring(48, 62).trim();
            storeName = line.substring(62).trim();
        } catch (NumberFormatException | IndexOutOfBoundsException | DateTimeParseException e) {
            throw new IllegalArgumentException("Erro ao processar a linha", e);
        }

        return TransactionFinance.builder()
                .type(type)
                .date(date)
                .value(value)
                .cpf(cpf)
                .card(card)
                .time(time)
                .storeOwner(storeOwner)
                .storeName(storeName)
                .build();
    }
}