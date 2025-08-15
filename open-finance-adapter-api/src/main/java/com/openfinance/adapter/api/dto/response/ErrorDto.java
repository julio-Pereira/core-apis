package com.openfinance.adapter.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalhes do erro")
public record ErrorDto(

        @Schema(description = "Código de erro específico do endpoint", maxLength = 255)
        @JsonProperty("code")
        String code,

        @Schema(description = "Título legível por humanos deste erro específico", maxLength = 255)
        @JsonProperty("title")
        String title,

        @Schema(description = "Descrição legível por humanos deste erro específico", maxLength = 2048)
        @JsonProperty("detail")
        String detail
) {}
