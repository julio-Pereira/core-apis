package com.openfinance.adapter.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response de erro")
public record ResponseErrorDto(

        @Schema(description = "Lista de erros")
        @JsonProperty("errors")
        List<ErrorDto> errors,

        @Schema(description = "Meta informações")
        @JsonProperty("meta")
        MetaDto meta
) {}
