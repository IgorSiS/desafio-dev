package com.desafio.dev.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum TransactionType {
    DEBIT(1, "Débito", "Entrada", "+"),
    BOLETO(2, "Boleto", "Saída", "-"),
    FINANCIAMENTO(3, "Financiamento", "Saída", "-"),
    CREDITO(4, "Crédito", "Entrada", "+"),
    RECEBIMENTO_EMPRESTIMO(5, "Recebimento Empréstimo", "Entrada", "+"),
    VENDAS(6, "Vendas", "Entrada", "+"),
    RECEBIMENTO_TED(7, "Recebimento TED", "Entrada", "+"),
    RECEBIMENTO_DOC(8, "Recebimento DOC", "Entrada", "+"),
    ALUGUEL(9, "Aluguel", "Saída", "-");

    private final int code;
    private final String description;
    private final String nature;
    private final String signal;

    private static final Map<Integer, TransactionType> codeMap = new HashMap<>();

    static {
        for (TransactionType type : TransactionType.values()) {
            codeMap.put(type.getCode(), type);
        }
    }

    public static TransactionType getByCode(int code) {
        return codeMap.get(code);
    }

    public int getCode() {
        return code;
    }
}