package com.openfinance.adapter.api.port;

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

    /**
     * Obtém a lista de contas do cliente
     */
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
                    description = "Data em que o usuário logou pela última vez com o receptor. Representada de acordo com a [RFC7231](https://tools.ietf.org/html/rfc7231). Exemplo: Sun, 10 Sep 2017 19:43:31 UTC",
                    required = false,
                    in = ParameterIn.HEADER,
                    schema = @Schema(type = "string", maxLength = 29, pattern = "^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), \\d{2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \\d{4} \\d{2}:\\d{2}:\\d{2} (GMT|UTC)$")
            )
            @RequestHeader(value = "x-fapi-auth-date", required = false) String xFapiAuthDate,

            @Parameter(
                    name = "x-fapi-customer-ip-address",
                    description = "O endereço IP do usuário se estiver atualmente logado com o receptor.",
                    required = false,
                    in = ParameterIn.HEADER,
                    schema = @Schema(type = "string", maxLength = 39, pattern = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$|^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$")
            )
            @RequestHeader(value = "x-fapi-customer-ip-address", required = false) String xFapiCustomerIpAddress,

            @Parameter(
                    name = "x-fapi-interaction-id",
                    description = "Um UUID [RFC4122](https://tools.ietf.org/html/rfc4122) usado como um ID de correlação entre request e response. Campo de geração e envio obrigatório pela receptora (client) e o seu valor deve ser \"espelhado\" pela transmissora (server) no cabeçalho de resposta.",
                    required = true,
                    in = ParameterIn.HEADER,
                    schema = @Schema(type = "string", format = "uuid", minLength = 1, maxLength = 36, pattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
            )
            @RequestHeader("x-fapi-interaction-id") String xFapiInteractionId,

            @Parameter(
                    name = "x-customer-user-agent",
                    description = "Indica o user-agent que o usuário utiliza.",
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
                    schema = @Schema(type = "integer", format = "int32", defaultValue = "1", minimum = "1", maximum = "2147483647")
            )
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,

            @Parameter(
                    name = "page-size",
                    description = "Quantidade total de registros por páginas.",
                    required = false,
                    in = ParameterIn.QUERY,
                    schema = @Schema(type = "integer", format = "int32", defaultValue = "25", minimum = "1", maximum = "1000")
            )
            @RequestParam(value = "page-size", required = false, defaultValue = "25") Integer pageSize,

            @Parameter(
                    name = "accountType",
                    description = "Tipos de contas. Modalidades tradicionais previstas pela Resolução 4.753, não contemplando contas vinculadas, conta de domiciliados no exterior, contas em moedas estrangeiras e conta correspondente moeda eletrônica.",
                    required = false,
                    in = ParameterIn.QUERY,
                    schema = @Schema(type = "string")
            )
            @RequestParam(value = "accountType", required = false) String accountType,

            @Parameter(
                    name = "pagination-key",
                    description = "Identificador de rechamada, utilizado para evitar a contagem de chamadas ao endpoint durante a paginação.",
                    required = false,
                    in = ParameterIn.QUERY,
                    schema = @Schema(type = "string", maxLength = 2048, pattern = "[\\w\\W\\s]*")
            )
            @RequestParam(value = "pagination-key", required = false) String paginationKey
    );
}
