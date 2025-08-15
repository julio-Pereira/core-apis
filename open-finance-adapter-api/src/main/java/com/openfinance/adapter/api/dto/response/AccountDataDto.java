package com.openfinance.adapter.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados da conta")
public record AccountDataDto(

        @Schema(description = "Identificador único da conta", example = "92792126019929279212650822221989319252576", maxLength = 100)
        @JsonProperty("accountId")
        String accountId,

        @Schema(description = "Tipo da conta", example = "CONTA_DEPOSITO_A_VISTA")
        @JsonProperty("type")
        String type,

        @Schema(description = "Subtipo da conta", example = "INDIVIDUAL")
        @JsonProperty("subtype")
        String subtype,

        @Schema(description = "Apelido da conta", maxLength = 50)
        @JsonProperty("nickname")
        String nickname,

        @Schema(description = "Empresa dona da conta")
        @JsonProperty("company")
        CompanyDto company,

        @Schema(description = "Número da conta", maxLength = 20)
        @JsonProperty("number")
        String number,

        @Schema(description = "Dígito verificador da conta", maxLength = 1)
        @JsonProperty("checkDigit")
        String checkDigit,

        @Schema(description = "Código da agência", maxLength = 4)
        @JsonProperty("branchCode")
        String branchCode,

        @Schema(description = "Dígito verificador da agência", maxLength = 1)
        @JsonProperty("branchCheckDigit")
        String branchCheckDigit
) {}