package com.openfinance.adapter.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Meta informações da resposta")
public record MetaDto(

        @Schema(description = "Número total de registros", example = "1")
        @JsonProperty("totalRecords")
        Integer totalRecords,

        @Schema(description = "Número total de páginas", example = "1")
        @JsonProperty("totalPages")
        Integer totalPages,

        @Schema(description = "Data e hora da consulta, conforme especificação RFC-3339, formato UTC", example = "2021-05-21T08:30:00Z")
        @JsonProperty("requestDateTime")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime requestDateTime
) {}

