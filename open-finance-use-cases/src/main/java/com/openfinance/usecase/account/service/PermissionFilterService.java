package com.openfinance.usecase.account.service;

import com.openfinance.core.domain.consent.OpenFinancePermission;
import com.openfinance.core.domain.consent.OpenFinancePermission.ProductGroup;
import com.openfinance.core.exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço responsável por filtrar e validar permissões conforme
 * especificações do Open Finance Brasil.
 *
 * Implementa as regras de:
 * - Filtragem de permissões suportadas pela instituição
 * - Manutenção de agrupamentos de produtos
 * - Validação de permissões funcionais mínimas
 * - Retorno de códigos HTTP apropriados
 */
@Slf4j
@Service
public class PermissionFilterService {

    /**
     * Filtra permissões retornando apenas o subconjunto suportado pela instituição.
     *
     * Regras implementadas:
     * 1. Remove permissões de produtos não suportados
     * 2. Mantém agrupamentos completos (Operações de Crédito, Investimentos, Câmbio)
     * 3. Valida se restam permissões funcionais
     *
     * @param requestedPermissions Lista de permissões solicitadas
     * @return Resultado da filtragem com permissões válidas e status
     * @throws ValidationException se não restarem permissões funcionais (HTTP 422)
     */
    public PermissionFilterResult filterSupportedPermissions(List<String> requestedPermissions) {
        log.debug("Filtering {} requested permissions", requestedPermissions.size());

        // Converter strings para enums, ignorando permissões inválidas
        Set<OpenFinancePermission> validPermissions = convertToPermissionEnums(requestedPermissions);

        if (validPermissions.isEmpty()) {
            log.warn("No valid permissions found in request: {}", requestedPermissions);
            throw ValidationException.with("INVALID_PERMISSIONS");
        }

        // Separar por grupos de produto
        Map<ProductGroup, Set<OpenFinancePermission>> groupedPermissions = groupPermissionsByProduct(validPermissions);

        // Aplicar filtragem por grupo
        Set<OpenFinancePermission> filteredPermissions = new HashSet<>();

        for (Map.Entry<ProductGroup, Set<OpenFinancePermission>> entry : groupedPermissions.entrySet()) {
            ProductGroup group = entry.getKey();
            Set<OpenFinancePermission> permissions = entry.getValue();

            if (group.isGrouped()) {
                // Para produtos agrupados, manter todas ou nenhuma
                filteredPermissions.addAll(filterGroupedProductPermissions(group, permissions));
            } else {
                // Para produtos individuais, filtrar apenas suportadas
                filteredPermissions.addAll(filterIndividualProductPermissions(permissions));
            }
        }

        // Validar se restam permissões funcionais
        validateFunctionalPermissions(filteredPermissions, requestedPermissions);

        // Determinar status HTTP
        HttpStatusResult statusResult = determineHttpStatus(validPermissions, filteredPermissions);

        log.info("Permission filtering completed: {} requested -> {} filtered, status: {}",
                requestedPermissions.size(), filteredPermissions.size(), statusResult.statusCode());

        return new PermissionFilterResult(
                convertToStringList(filteredPermissions),
                statusResult.statusCode(),
                statusResult.message(),
                calculateRemovedPermissions(validPermissions, filteredPermissions)
        );
    }

    /**
     * Converte strings para enums de permissão, ignorando inválidas
     */
    private Set<OpenFinancePermission> convertToPermissionEnums(List<String> permissionStrings) {
        return permissionStrings.stream()
                .map(OpenFinancePermission::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Agrupa permissões por produto
     */
    private Map<ProductGroup, Set<OpenFinancePermission>> groupPermissionsByProduct(
            Set<OpenFinancePermission> permissions) {

        return permissions.stream()
                .collect(Collectors.groupingBy(
                        OpenFinancePermission::getProductGroup,
                        Collectors.toSet()
                ));
    }

    /**
     * Filtra permissões de produtos agrupados
     * Regra: Manter todas as permissões do grupo se pelo menos uma for suportada
     */
    private Set<OpenFinancePermission> filterGroupedProductPermissions(
            ProductGroup group, Set<OpenFinancePermission> permissions) {

        log.debug("Filtering grouped product permissions for group: {}", group.getDescription());

        // Verificar se alguma permissão do grupo é suportada
        boolean hasAnySupported = permissions.stream()
                .anyMatch(OpenFinancePermission::isSupported);

        if (hasAnySupported) {
            log.debug("Group {} has supported permissions, maintaining all permissions in group",
                    group.getDescription());
            return permissions; // Manter todas as permissões do agrupamento
        } else {
            log.debug("Group {} has no supported permissions, removing all", group.getDescription());
            return Set.of(); // Remover todas as permissões do agrupamento
        }
    }

    /**
     * Filtra permissões de produtos individuais
     * Regra: Manter apenas as permissões suportadas
     */
    private Set<OpenFinancePermission> filterIndividualProductPermissions(
            Set<OpenFinancePermission> permissions) {

        log.debug("Filtering individual product permissions");

        Set<OpenFinancePermission> supported = permissions.stream()
                .filter(OpenFinancePermission::isSupported)
                .collect(Collectors.toSet());

        log.debug("Individual permissions: {} requested -> {} supported",
                permissions.size(), supported.size());

        return supported;
    }

    /**
     * Valida se restam permissões funcionais após filtragem
     * Permissões de RESOURCES não são consideradas funcionais
     */
    private void validateFunctionalPermissions(Set<OpenFinancePermission> filteredPermissions,
                                               List<String> originalRequest) {

        boolean hasFunctionalPermissions = filteredPermissions.stream()
                .anyMatch(OpenFinancePermission::isFunctional);

        if (!hasFunctionalPermissions) {
            log.error("No functional permissions remaining after filtering. Original request: {}",
                    originalRequest);
            throw ValidationException.with("NO_FUNCTIONAL_PERMISSIONS");
        }

        log.debug("Functional permissions validation passed: {} functional permissions found",
                filteredPermissions.stream().mapToLong(p -> p.isFunctional() ? 1 : 0).sum());
    }

    /**
     * Determina o código HTTP de resposta
     * - 201: Permissões filtradas com sucesso
     * - 422: Não restaram permissões funcionais
     */
    private HttpStatusResult determineHttpStatus(Set<OpenFinancePermission> original,
                                                 Set<OpenFinancePermission> filtered) {

        if (filtered.isEmpty()) {
            return new HttpStatusResult(422, "Nenhuma permissão suportada encontrada");
        }

        if (original.size() > filtered.size()) {
            return new HttpStatusResult(201,
                    "Permissões filtradas - produtos não suportados removidos");
        }

        return new HttpStatusResult(201, "Todas as permissões são suportadas");
    }

    /**
     * Converte enums de volta para lista de strings
     */
    private List<String> convertToStringList(Set<OpenFinancePermission> permissions) {
        return permissions.stream()
                .map(OpenFinancePermission::getPermission)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Calcula permissões removidas para logging
     */
    private List<String> calculateRemovedPermissions(Set<OpenFinancePermission> original,
                                                     Set<OpenFinancePermission> filtered) {

        Set<OpenFinancePermission> removed = new HashSet<>(original);
        removed.removeAll(filtered);

        return convertToStringList(removed);
    }

    /**
     * Verifica se um conjunto de permissões é válido para uma operação específica
     *
     * @param permissions Lista de permissões do consentimento
     * @param requiredPermission Permissão mínima necessária
     * @return true se tem a permissão necessária, false caso contrário
     */
    public boolean hasRequiredPermission(List<String> permissions, OpenFinancePermission requiredPermission) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        return permissions.contains(requiredPermission.getPermission());
    }

    /**
     * Verifica se tem pelo menos uma permissão de um conjunto
     */
    public boolean hasAnyPermission(List<String> permissions, Set<OpenFinancePermission> requiredPermissions) {
        if (permissions == null || permissions.isEmpty() || requiredPermissions == null) {
            return false;
        }

        Set<String> requiredStrings = requiredPermissions.stream()
                .map(OpenFinancePermission::getPermission)
                .collect(Collectors.toSet());

        return permissions.stream()
                .anyMatch(requiredStrings::contains);
    }

    /**
     * Obtém permissões suportadas pela instituição
     */
    public List<String> getSupportedPermissions() {
        return OpenFinancePermission.getSupportedPermissions().stream()
                .map(OpenFinancePermission::getPermission)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Record com resultado da filtragem de permissões
     */
    public record PermissionFilterResult(
            List<String> filteredPermissions,
            int httpStatusCode,
            String message,
            List<String> removedPermissions
    ) {

        public boolean hasPermissions() {
            return filteredPermissions != null && !filteredPermissions.isEmpty();
        }

        public boolean wasFiltered() {
            return removedPermissions != null && !removedPermissions.isEmpty();
        }

        public int getPermissionCount() {
            return filteredPermissions != null ? filteredPermissions.size() : 0;
        }
    }

    /**
     * Record com resultado de status HTTP
     */
    private record HttpStatusResult(
            int statusCode,
            String message
    ) {}
}
