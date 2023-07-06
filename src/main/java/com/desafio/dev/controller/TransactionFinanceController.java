package com.desafio.dev.controller;

import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.gateway.exception.GatewayException;
import com.desafio.dev.usecase.FindTransactionByNameUseCase;
import com.desafio.dev.usecase.TransactionFinanceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionFinanceController {

    private final TransactionFinanceUseCase transactionFinanceUseCase;
    private final FindTransactionByNameUseCase findTransactionByNameUseCase;


    @PostMapping("/process/data")
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(summary = "Exportar operações financeiras")
    public List<TransactionFinance> processTransactionFinance(@RequestParam("file") MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return transactionFinanceUseCase.execute(inputStream);
        }
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "Consultar operações financeiras por loja")
    public Page<TransactionFinance> findTransactionByStore(@RequestParam(value = "storeName", required = false) String storeName,
                                                           @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                           @RequestParam(value = "size", defaultValue = "15") Integer size) throws GatewayException {
        return findTransactionByNameUseCase.execute(storeName, page, size);
    }
}