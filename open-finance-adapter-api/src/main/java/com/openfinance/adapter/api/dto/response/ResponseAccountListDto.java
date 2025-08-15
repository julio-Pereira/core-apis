package com.openfinance.adapter.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response com lista de contas")
public record ResponseAccountListDto(

        @Schema(description = "Lista de contas depósito à vista, poupança e pagamento pré-pagas")
        @JsonProperty("data")
        List<AccountDataDto> data,

        @Schema(description = "Links de navegação")
        @JsonProperty("links")
        LinksDto links,

        @Schema(description = "Meta informações")
        @JsonProperty("meta")
        MetaDto meta
) {}
