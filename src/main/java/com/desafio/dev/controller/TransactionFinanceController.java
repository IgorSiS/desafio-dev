package com.desafio.dev.controller;

import com.desafio.dev.controller.response.TransactionFinanceResponse;
import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.domain.exception.UseCaseException;
import com.desafio.dev.mapper.TransactionMapper;
import com.desafio.dev.usecase.FindTransactionByNameUseCase;
import com.desafio.dev.usecase.ExtractTransactionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionFinanceController {

    private final ExtractTransactionUseCase extractTransactionUseCase;
    private final FindTransactionByNameUseCase findTransactionByNameUseCase;
    private final TransactionMapper transactionMapper;

    @PostMapping("/process/data")
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(summary = "Exportar operações financeiras")
    public List<TransactionFinance> processTransactionFinance(@RequestParam("file") MultipartFile file) throws IOException, UseCaseException {
        return extractTransactionUseCase.execute(file);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "Consultar operações financeiras por loja")
    public TransactionFinanceResponse findTransactionByStore(@RequestParam(value = "storeName", required = false) String storeName,
                                                             @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                             @RequestParam(value = "size", defaultValue = "15") Integer size) throws UseCaseException {

        Page<TransactionFinance> transactionPages = findTransactionByNameUseCase.execute(storeName, page, size);
        Double accountBalance = transactionPages.stream()
                .mapToDouble(TransactionFinance::getValue)
                .sum();

        TransactionFinanceResponse response = transactionMapper.toTransactionFinanceResponse(transactionPages);
        response.setAccountBalance(accountBalance);

        return response;
    }
}