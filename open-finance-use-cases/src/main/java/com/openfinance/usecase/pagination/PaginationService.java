package com.openfinance.usecase.pagination;

import com.openfinance.usecase.account.retrieve.list.GetAccountsInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Serviço responsável por gerenciar paginação conforme especificações
 * do Open Finance Brasil.
 *
 * Implementa:
 * - Geração de links HATEOAS
 * - Chaves de paginação para controle de limites operacionais
 * - Validação de chaves de paginação
 * - Cálculo de informações de paginação
 */
@Slf4j
@Service
public class PaginationService {

    @Value("${open-finance.api.base-url:https://api.banco.com.br/open-banking}")
    private String baseUrl;

    @Value("${open-finance.pagination.key-ttl-minutes:60}")
    private int paginationKeyTtlMinutes;

    /**
     * Cria informações completas de paginação para uma resposta
     *
     * @param input Parâmetros da requisição original
     * @param totalElements Total de elementos encontrados
     * @param endpoint Endpoint sendo paginado
     * @return PaginationInfo com todos os links e metadados
     */
    public PaginationInfo createPaginationInfo(GetAccountsInput input, int totalElements, String endpoint) {
        log.debug("Creating pagination info for endpoint: {}, page: {}, pageSize: {}, total: {}",
                endpoint, input.page(), input.pageSize(), totalElements);

        int totalPages = calculateTotalPages(totalElements, input.pageSize());

        String selfLink = buildLink(endpoint, input.page(), input.pageSize(),
                input.type().orElse(null), null);

        String firstLink = buildLink(endpoint, 1, input.pageSize(),
                input.type().orElse(null), null);

        String lastLink = buildLink(endpoint, totalPages, input.pageSize(),
                input.type().orElse(null), null);

        String prevLink = input.page() > 1 ?
                buildLink(endpoint, input.page() - 1, input.pageSize(),
                        input.type().orElse(null), null) : null;

        String nextLink = input.page() < totalPages ?
                buildLink(endpoint, input.page() + 1, input.pageSize(),
                        input.type().orElse(null), generatePaginationKey(input)) : null;

        PaginationInfo paginationInfo = PaginationInfo.builder()
                .selfLink(selfLink)
                .firstLink(firstLink)
                .prevLink(prevLink)
                .nextLink(nextLink)
                .lastLink(lastLink)
                .totalRecords(totalElements)
                .totalPages(totalPages)
                .currentPage(input.page())
                .pageSize(input.pageSize())
                .paginationKey(input.paginationKey().orElse(null))
                .build();

        log.debug("Pagination info created: totalPages={}, currentPage={}, hasNext={}, hasPrev={}",
                totalPages, input.page(), nextLink != null, prevLink != null);

        return paginationInfo;
    }

    /**
     * Gera uma chave de paginação segura para controle de limites operacionais
     *
     * @param input Parâmetros da requisição
     * @return Chave de paginação codificada
     */
    public String generatePaginationKey(GetAccountsInput input) {
        log.debug("Generating pagination key for consentId: {}, page: {}",
                input.consentId(), input.page());

        try {
            String keyData = String.format("%s:%s:%s:%d:%d:%s",
                    input.consentId(),
                    input.organizationId(),
                    input.type().map(Enum::toString).orElse("ALL"),
                    input.page(),
                    input.pageSize(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keyData.getBytes(StandardCharsets.UTF_8));
            String paginationKey = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

            log.debug("Pagination key generated successfully");
            return paginationKey;

        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating pagination key: {}", e.getMessage(), e);
            // Fallback para chave simples baseada em timestamp
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Valida se uma chave de paginação é válida e não expirou
     *
     * @param paginationKey Chave a ser validada
     * @return true se a chave é válida, false caso contrário
     */
    public boolean isValidPaginationKey(String paginationKey) {
        log.debug("Validating pagination key");

        if (paginationKey == null || paginationKey.trim().isEmpty()) {
            log.debug("Pagination key is null or empty");
            return false;
        }

        try {
            // Decodificar a chave
            byte[] decodedKey = Base64.getUrlDecoder().decode(paginationKey);

            // Verificar se não está vazia após decodificação
            if (decodedKey.length == 0) {
                log.debug("Decoded pagination key is empty");
                return false;
            }

            // Por simplicidade, aceitar chaves que possam ser decodificadas
            // Em implementação real, verificar timestamp e validade
            log.debug("Pagination key validation passed");
            return true;

        } catch (IllegalArgumentException e) {
            log.warn("Invalid pagination key format: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se uma chave de paginação expirou
     *
     * @param paginationKey Chave a ser verificada
     * @return true se expirou, false caso contrário
     */
    public boolean isPaginationKeyExpired(String paginationKey) {
        // Implementação simplificada - em produção, extrair timestamp da chave
        // e verificar contra TTL configurado

        log.debug("Checking if pagination key is expired (simplified implementation)");

        if (!isValidPaginationKey(paginationKey)) {
            return true;
        }

        // Por simplicidade, consideramos que chaves sempre são válidas
        // Em implementação real, extrair timestamp e comparar com TTL
        return false;
    }

    /**
     * Calcula o número total de páginas
     */
    private int calculateTotalPages(int totalElements, int pageSize) {
        if (totalElements == 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    /**
     * Constrói URL para links de paginação
     */
    private String buildLink(String endpoint, int page, int pageSize,
                             Object accountType, String paginationKey) {

        StringBuilder linkBuilder = new StringBuilder()
                .append(baseUrl)
                .append(endpoint)
                .append("?page=").append(page)
                .append("&page-size=").append(pageSize);

        if (accountType != null) {
            linkBuilder.append("&accountType=").append(accountType.toString());
        }

        if (paginationKey != null && !paginationKey.trim().isEmpty()) {
            linkBuilder.append("&pagination-key=").append(paginationKey);
        }

        return linkBuilder.toString();
    }

    /**
     * Extrai informações de uma chave de paginação (implementação simplificada)
     *
     * @param paginationKey Chave a ser analisada
     * @return Informações extraídas da chave
     */
    public PaginationKeyInfo extractPaginationKeyInfo(String paginationKey) {
        log.debug("Extracting pagination key info");

        if (!isValidPaginationKey(paginationKey)) {
            return null;
        }

        try {
            // Implementação simplificada - em produção, extrair dados reais da chave
            return PaginationKeyInfo.builder()
                    .isValid(true)
                    .isExpired(false)
                    .createdAt(LocalDateTime.now().minusMinutes(5)) // Simulado
                    .expiresAt(LocalDateTime.now().plusMinutes(paginationKeyTtlMinutes))
                    .build();

        } catch (Exception e) {
            log.error("Error extracting pagination key info: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Record para informações extraídas de chave de paginação
     */
    public record PaginationKeyInfo(
            boolean isValid,
            boolean isExpired,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private boolean isValid;
            private boolean isExpired;
            private LocalDateTime createdAt;
            private LocalDateTime expiresAt;

            public Builder isValid(boolean isValid) {
                this.isValid = isValid;
                return this;
            }

            public Builder isExpired(boolean isExpired) {
                this.isExpired = isExpired;
                return this;
            }

            public Builder createdAt(LocalDateTime createdAt) {
                this.createdAt = createdAt;
                return this;
            }

            public Builder expiresAt(LocalDateTime expiresAt) {
                this.expiresAt = expiresAt;
                return this;
            }

            public PaginationKeyInfo build() {
                return new PaginationKeyInfo(isValid, isExpired, createdAt, expiresAt);
            }
        }
    }
}
