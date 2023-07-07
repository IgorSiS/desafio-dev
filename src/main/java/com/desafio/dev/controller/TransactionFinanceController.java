package com.desafio.dev.controller;

import com.desafio.dev.controller.response.FindTransactionResponse;
import com.desafio.dev.domain.TransactionFinance;
import com.desafio.dev.domain.exception.UseCaseException;
import com.desafio.dev.mapper.TransactionMapper;
import com.desafio.dev.usecase.ExtractTransactionUseCase;
import com.desafio.dev.usecase.FindTransactionByNameUseCase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @ResponseStatus(HttpStatus.CREATED)
    public List<FindTransactionResponse> processTransactionFinance(@RequestParam("file") MultipartFile file)
            throws UseCaseException {
        List<TransactionFinance> transactionFinances = extractTransactionUseCase.execute(file);
        return transactionMapper.toFindTransactionResponse(transactionFinances);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "Consultar operações financeiras por loja")
    public List<FindTransactionResponse> findTransactionByStore(@RequestParam(value = "storeName", required = false) String storeName)
            throws UseCaseException {
        List<TransactionFinance> resposne = findTransactionByNameUseCase.execute(storeName);
        return transactionMapper.toFindTransactionResponse(resposne);
    }
}