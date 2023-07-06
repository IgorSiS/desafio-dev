package com.desafio.dev.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("codigo")
    protected Integer code;

    @JsonProperty("mensagem")
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("titulo")
    private String title;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("erros")
    private List<String> errors;

    public void addError(final String error){
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }
}