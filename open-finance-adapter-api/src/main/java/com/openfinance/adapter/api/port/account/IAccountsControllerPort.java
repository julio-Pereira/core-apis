package com.openfinance.adapter.api.port.account;

import com.openfinance.adapter.api.dto.response.ResponseAccountListDto;
import com.openfinance.adapter.api.dto.response.ResponseErrorDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

/**
 * Port interface for Accounts Controller operations
 *
 * This interface defines the contract for all Accounts API endpoints
 * following Open Finance specifications and hexagonal architecture principles.
 */
@Tag(name = "Accounts", description = "API de Contas do Open Finance")
public interface IAccountsControllerPort {

    @Operation(
            summary = "Obtém a lista de contas depósito à vista, poupança e pagamento pré-pagas",
            description = "Método para obter a lista de contas depósito à vista, poupança e pagamento pré-pagas " +
                    "mantidas pelo cliente na instituição transmissora e para as quais ele tenha fornecido consentimento.",
            operationId = "accountsGetAccounts"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dados de identificação das contas obtidos com sucesso.",
                    headers = {
                            @Header(
                                    name = "x-fapi-interaction-id",
                                    required = true,
                                    description = "Um UUID [RFC4122](https://tools.ietf.org/html/rfc4122) usado como um ID de correlação entre request e response.",
                                    schema = @Schema(type = "string", format = "uuid")
                            )
                    },
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseAccountListDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "A requisição foi malformada, omitindo atributos obrigatórios, seja no payload ou através de atributos na URL.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Cabeçalho de autenticação ausente/inválido ou token inválido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "O token tem escopo incorreto ou uma política de segurança foi violada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "O recurso solicitado não existe ou não foi implementado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "405",
                    description = "O consumidor tentou acessar o recurso com um método não suportado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "406",
                    description = "A solicitação continha um cabeçalho Accept diferente dos tipos de mídia permitidos ou um conjunto de caracteres diferente de UTF-8",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "A sintaxe da requisição esta correta, mas não foi possível processar as instruções presentes",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "423",
                    description = "Recurso bloqueado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "A operação foi recusada, pois muitas solicitações foram feitas dentro de um determinado período ou o limite de TPS foi atingido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Ocorreu um erro no gateway da API ou no microsserviço",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "504",
                    description = "Gateway timeout",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "529",
                    description = "O site está sobrecarregado e a operação foi recusada, pois foi atingido o limite máximo de TPS global, neste momento.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseErrorDto.class)
                    )
            )
    })
    @SecurityRequirement(name = "OpenId", scopes = {"openid"})
    @SecurityRequirement(name = "OAuth2Security", scopes = {"consent:consentId", "accounts"})
    @GetMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<ResponseAccountListDto>> getAccounts(

            @Parameter(
                    name = "Authorization",
                    description = "Cabeçalho HTTP padrão. Permite que as credenciais sejam fornecidas dependendo do tipo de recurso solicitado",
                    required = true,
                    in = ParameterIn.HEADER,
                    schema = @Schema(type = "string", maxLength = 2048, pattern = "[\\w\\W\\s]*")
            )
            @RequestHeader("Authorization") String authorization,

            @Parameter(
                    name = "x-fapi-auth-date",
                    description = "Data em que o usuário logou pela última vez com o receptor. Representada de acordo com a [RFC7231](https://tools.ietf.org/html/rfc7231).",
                    required = false,
                    in = ParameterIn.HEADER,
                    schema = @Schema(type = "string", maxLength = 255, pattern = "[\\w\\W\\s]*")
            )
            @RequestHeader(value = "x-fapi-auth-date", required = false) String xFapiAuthDate,

            @Parameter(
                    name = "x-fapi-customer-ip-address",
                    description = "O endereço IP do usuário se estiver atualmente logado com o receptor.",
                    required = false,
                    in = ParameterIn.HEADER,
                    schema = @Schema(type = "string", maxLength = 255, pattern = "[\\w\\W\\s]*")
            )
            @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,

            @Parameter(
                    name = "x-fapi-interaction-id",
                    description = "Um UUID [RFC4122](https://tools.ietf.org/html/rfc4122) usado como um ID de correlação entre request e response.",
                    required = true,
                    in = ParameterIn.HEADER,
                    schema = @Schema(type = "string", format = "uuid")
            )
            @RequestHeader("x-fapi-interaction-id") String xFapiInteractionId,

            @Parameter(
                    name = "x-customer-user-agent",
                    description = "Indica o user-agent que o usuário está utilizando.",
                    required = false,
                    in = ParameterIn.HEADER,
                    schema = @Schema(type = "string", maxLength = 100, pattern = "[\\w\\W\\s]*")
            )
            @RequestHeader(value = "x-customer-user-agent", required = false) String xCustomerUserAgent,

            @Parameter(
                    name = "page",
                    description = "Número da página que está sendo requisitada (o valor da primeira página é 1).",
                    required = false,
                    in = ParameterIn.QUERY,
                    schema = @Schema(type = "integer", minimum = "1", maximum = "2147483647", format = "int32", defaultValue = "1")
            )
            @RequestParam(value = "page", required = false) Integer page,

            @Parameter(
                    name = "page-size",
                    description = "Quantidade total de registros por páginas.",
                    required = false,
                    in = ParameterIn.QUERY,
                    schema = @Schema(type = "integer", minimum = "1", maximum = "1000", format = "int32", defaultValue = "25")
            )
            @RequestParam(value = "page-size", required = false) Integer pageSize,

            @Parameter(
                    name = "accountType",
                    description = "Tipo de conta consultada.",
                    required = false,
                    in = ParameterIn.QUERY,
                    schema = @Schema(type = "string", allowableValues = {
                            "CONTA_DEPOSITO_A_VISTA",
                            "CONTA_POUPANCA",
                            "CONTA_PAGAMENTO_PRE_PAGA"
                    })
            )
            @RequestParam(value = "accountType", required = false) String accountType,

            @Parameter(
                    name = "pagination-key",
                    description = "Chave de paginação para controle de limites operacionais e identificação de consultas.",
                    required = false,
                    in = ParameterIn.QUERY,
                    schema = @Schema(type = "string", maxLength = 255)
            )
            @RequestParam(value = "pagination-key", required = false) String paginationKey
    );
}