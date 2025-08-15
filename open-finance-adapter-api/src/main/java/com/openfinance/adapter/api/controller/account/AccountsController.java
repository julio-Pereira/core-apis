package com.openfinance.adapter.api.controller.account;

import com.openfinance.adapter.api.dto.response.ResponseAccountListDto;
import com.openfinance.adapter.api.mapper.IAccountsApiMapper;
import com.openfinance.adapter.api.port.account.IAccountsControllerPort;
import com.openfinance.core.domain.account.AccountType;
import com.openfinance.usecase.account.retrieve.list.GetAccountsFacade;
import com.openfinance.usecase.account.retrieve.list.GetAccountsInput;
import com.openfinance.usecase.account.retrieve.list.GetAccountsOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller implementando os endpoints de Accounts conforme especificação Open Finance Brasil.
 *
 * Este controller implementa o padrão hexagonal, delegando a lógica de negócio para o use case
 * através do facade e utilizando o mapper para conversão entre DTOs da API e do Use Case.
 *
 * @see IAccountsControllerPort
 * @see GetAccountsFacade
 * @see IAccountsApiMapper
 */
@Slf4j
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountsController implements IAccountsControllerPort {

    private final GetAccountsFacade getAccountsFacade;
    private final IAccountsApiMapper mapper;


    /**
     * {@inheritDoc}
     *
     * Implementa o endpoint GET /accounts seguindo as especificações do Open Finance Brasil.
     * Aplica validações FAPI, rate limiting, consentimento e retorna dados paginados.
     */
    @Override
    public Mono<ResponseEntity<ResponseAccountListDto>> getAccounts(
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            Integer page,
            Integer pageSize,
            String accountType,
            String paginationKey) {

        log.debug("Processing GET /accounts request - interactionId: {}, page: {}, pageSize: {}, accountType: {}",
                xFapiInteractionId, page, pageSize, accountType);

        return Mono.fromCallable(() -> {
                    // Extrair consentId do Authorization header (JWT token)
                    String consentId = extractConsentIdFromJwt(authorization);

                    // Extrair organizationId do Authorization header
                    String organizationId = extractOrganizationIdFromJwt(authorization);

                    // Construir input para o use case
                    GetAccountsInput input = GetAccountsInput.builder()
                            .consentId(consentId)
                            .organizationId(organizationId)
                            .xFapiInteractionId(xFapiInteractionId)
                            .xFapiAuthDate(parseOptionalOffsetDateTime(xFapiAuthDate).toString())
                            .xFapiCustomerIpAddress(Optional.ofNullable(xFapiCustomerIpAddress).toString())
                            .xCustomerUserAgent(Optional.ofNullable(xCustomerUserAgent).toString())
                            .page(Optional.ofNullable(page).orElse(1))
                            .pageSize(Optional.ofNullable(pageSize).orElse(25))
                            .type(parseAccountType(accountType))
                            .paginationKey(Optional.ofNullable(paginationKey).toString())
                            .build();

                    // Executar use case
                    GetAccountsOutput output = getAccountsFacade.getAccounts(input);

                    // Mapear para DTO de resposta
                    ResponseAccountListDto response = mapper.toResponseAccountListDto(output);

                    // Construir headers de resposta
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("x-fapi-interaction-id", xFapiInteractionId);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(response);
                })
                .doOnNext(response -> log.debug("Successfully processed GET /accounts - interactionId: {}, accounts returned: {}",
                        xFapiInteractionId, response.getBody().data().size()))
                .doOnError(error -> log.error("Error processing GET /accounts - interactionId: {}, error: {}",
                        xFapiInteractionId, error.getMessage(), error));
    }

    /**
     * Extrai o consentId do token JWT presente no header Authorization
     *
     * @param authorization Header Authorization contendo o JWT token
     * @return consentId extraído do token
     */
    private String extractConsentIdFromJwt(String authorization) {
        // TODO: Implementar extração real do JWT token
        // Por enquanto, simulação para desenvolvimento
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        // Implementação simplificada - em produção usar biblioteca JWT
        // para decodificar o token e extrair o claim do consentId
        return "consent-" + UUID.randomUUID().toString();
    }

    /**
     * Extrai o organizationId do token JWT presente no header Authorization
     *
     * @param authorization Header Authorization contendo o JWT token
     * @return organizationId extraído do token
     */
    private String extractOrganizationIdFromJwt(String authorization) {
        // TODO: Implementar extração real do JWT token
        // Por enquanto, simulação para desenvolvimento
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        // Implementação simplificada - em produção usar biblioteca JWT
        // para decodificar o token e extrair o claim do organizationId
        return "org-" + UUID.randomUUID().toString();
    }

    /**
     * Converte string do tipo de conta para enum AccountType
     *
     * @param accountType String representando o tipo de conta
     * @return Optional contendo o AccountType se válido
     */
    private AccountType parseAccountType(String accountType) {
        if (accountType == null || accountType.trim().isEmpty()) {
            return null;
        }

        try {
            return AccountType.valueOf(accountType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid account type received: {}", accountType);
            throw new IllegalArgumentException("Invalid account type: " + accountType);
        }
    }

    /**
     * Converte string de data em OffsetDateTime
     *
     * @param dateString String representando a data
     * @return Optional contendo OffsetDateTime se válido
     */
    private Optional<OffsetDateTime> parseOptionalOffsetDateTime(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(OffsetDateTime.parse(dateString));
        } catch (Exception e) {
            log.warn("Invalid date format received: {}", dateString);
            return Optional.empty();
        }
    }
}
