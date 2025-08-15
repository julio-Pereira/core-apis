package com.openfinance.adapter.api.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados da empresa")
public record CompanyDto(

        @Schema(description = "Nome da empresa", example = "Organização A", maxLength = 70)
        @JsonProperty("name")
        String name,

        @Schema(description = "CNPJ da empresa", example = "21128159000166", maxLength = 14)
        @JsonProperty("cnpjNumber")
        String cnpjNumber
) {}
