package com.openfinance.adapter.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Links de navegação para paginação")
public record LinksDto(

        @Schema(description = "URI para a própria página", example = "https://api.banco.com.br/open-banking/accounts/v2/accounts")
        @JsonProperty("self")
        String self,

        @Schema(description = "URI para a primeira página")
        @JsonProperty("first")
        String first,

        @Schema(description = "URI para a página anterior")
        @JsonProperty("prev")
        String prev,

        @Schema(description = "URI para a próxima página")
        @JsonProperty("next")
        String next,

        @Schema(description = "URI para a última página")
        @JsonProperty("last")
        String last
) {}
