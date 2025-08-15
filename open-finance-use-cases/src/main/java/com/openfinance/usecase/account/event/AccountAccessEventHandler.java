package com.openfinance.usecase.account.event;

import com.openfinance.core.events.account.AccountAccessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Handler para processar eventos de acesso a contas.
 *
 * Responsável por:
 * - Auditoria e compliance
 * - Métricas de negócio
 * - Alertas de segurança
 * - Persistência para análise posterior
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountAccessEventHandler {

    private final AuditService auditService;
    private final MetricsService metricsService;
    private final SecurityMonitoringService securityService;
    private final ComplianceReportingService complianceService;

    /**
     * Processa evento de acesso a contas de forma assíncrona
     */
    @Async
    @EventListener
    public void handleAccountAccessEvent(AccountAccessedEvent event) {
        log.debug("Processing AccountAccessedEvent: {}", event.getEventId());

        try {
            // 1. Registrar auditoria
            registerAuditLog(event);

            // 2. Coletar métricas de negócio
            collectBusinessMetrics(event);

            // 3. Verificar segurança
            performSecurityChecks(event);

            // 4. Registrar compliance
            recordComplianceMetrics(event);

            // 5. Detectar anomalias
            detectAnomalies(event);

            log.debug("AccountAccessedEvent processed successfully: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Error processing AccountAccessedEvent {}: {}",
                    event.getEventId(), e.getMessage(), e);
        }
    }

    /**
     * Registra log estruturado para auditoria
     */
    private void registerAuditLog(AccountAccessedEvent event) {
        log.info("ACCOUNT_ACCESS_AUDIT - EventId: {}, ConsentId: {}, OrganizationId: {}, " +
                        "Operation: {}, Success: {}, AccountCount: {}, PermissionsFiltered: {}, " +
                        "RemovedPermissions: {}, ExecutionTimeMs: {}, InteractionId: {}, " +
                        "UserAgent: {}, CustomerIp: {}, Timestamp: {}",
                event.getEventId(),
                event.getConsentId(),
                event.getOrganizationId(),
                event.getOperation(),
                event.isSuccessful(),
                event.getAccountCount(),
                event.wasPermissionFiltered(),
                event.getRemovedPermissions(),
                event.getAccessResult().executionTimeMs(),
                event.getAccessContext().xFapiInteractionId(),
                event.getAccessContext().xCustomerUserAgent(),
                event.getAccessContext().xFapiCustomerIpAddress(),
                event.getOccurredOn().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Persistir para auditoria de longo prazo
        auditService.persistAuditRecord(createAuditRecord(event));
    }

    /**
     * Coleta métricas de negócio
     */
    private void collectBusinessMetrics(AccountAccessedEvent event) {
        // Métricas de uso por organização
        metricsService.incrementCounter("account_access_total",
                "organization_id", event.getOrganizationId(),
                "success", String.valueOf(event.isSuccessful()));

        // Métricas de filtragem de permissões
        if (event.wasPermissionFiltered()) {
            metricsService.incrementCounter("permission_filtering_applied",
                    "organization_id", event.getOrganizationId(),
                    "removed_count", String.valueOf(event.getRemovedPermissions().size()));
        }

        // Métricas de SLA
        metricsService.recordTimer("account_access_duration",
                event.getAccessResult().executionTimeMs(),
                "within_sla", String.valueOf(event.isWithinSLA()),
                "organization_id", event.getOrganizationId());

        // Distribuição de contas por acesso
        metricsService.recordHistogram("accounts_per_access",
                event.getAccountCount(),
                "organization_id", event.getOrganizationId());
    }

    /**
     * Realiza verificações de segurança
     */
    private void performSecurityChecks(AccountAccessedEvent event) {
        var securityContext = event.getComplianceInfo().securityContext();

        // Verificar atividade suspeita
        if (securityContext.suspiciousActivity()) {
            securityService.reportSuspiciousActivity(
                    event.getConsentId(),
                    event.getOrganizationId(),
                    event.getAccessContext().xFapiCustomerIpAddress(),
                    "Suspicious activity detected during account access"
            );
        }

        // Verificar IP não permitido
        if (!securityContext.ipAddressAllowed()) {
            securityService.reportUnauthorizedIpAccess(
                    event.getConsentId(),
                    event.getAccessContext().xFapiCustomerIpAddress(),
                    event.getAccessContext().xFapiInteractionId()
            );
        }

        // Verificar flags de segurança
        if (securityContext.hasSecurityIssues()) {
            securityService.escalateSecurityAlert(
                    event.getConsentId(),
                    securityContext.securityFlags()
            );
        }
    }

    /**
     * Registra métricas de compliance
     */
    private void recordComplianceMetrics(AccountAccessedEvent event) {
        var complianceInfo = event.getComplianceInfo();

        // SLA compliance
        complianceService.recordSLACompliance(
                event.getEndpoint(),
                event.getOrganizationId(),
                complianceInfo.withinSLA(),
                event.getAccessResult().executionTimeMs()
        );

        // Rate limiting compliance
        if (complianceInfo.isRateLimitNearExhaustion()) {
            complianceService.alertRateLimitNearExhaustion(
                    event.getOrganizationId(),
                    complianceInfo.remainingRateLimit()
            );
        }

        // Permission filtering compliance
        if (event.wasPermissionFiltered()) {
            complianceService.recordPermissionFiltering(
                    event.getOrganizationId(),
                    event.getOriginalPermissions(),
                    event.getFilteredPermissions(),
                    event.getRemovedPermissions()
            );
        }

        // Audit level compliance
        complianceService.recordAuditLevel(
                event.getConsentId(),
                complianceInfo.auditLevel()
        );
    }

    /**
     * Detecta anomalias e padrões suspeitos
     */
    private void detectAnomalies(AccountAccessedEvent event) {
        // Múltiplos acessos em período curto
        if (detectHighFrequencyAccess(event)) {
            securityService.reportHighFrequencyAccess(
                    event.getConsentId(),
                    event.getOrganizationId()
            );
        }

        // Acessos fora do horário normal
        if (detectOffHoursAccess(event)) {
            securityService.reportOffHoursAccess(
                    event.getConsentId(),
                    event.getOccurredOn()
            );
        }

        // Padrão incomum de permissões removidas
        if (detectUnusualPermissionPattern(event)) {
            complianceService.reportUnusualPermissionPattern(
                    event.getOrganizationId(),
                    event.getRemovedPermissions()
            );
        }
    }

    /**
     * Cria registro de auditoria estruturado
     */
    private AuditRecord createAuditRecord(AccountAccessedEvent event) {
        return AuditRecord.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .aggregateId(event.getAggregateId())
                .occurredOn(event.getOccurredOn())
                .consentId(event.getConsentId())
                .organizationId(event.getOrganizationId())
                .operation(event.getOperation())
                .endpoint(event.getEndpoint())
                .success(event.isSuccessful())
                .accountCount(event.getAccountCount())
                .executionTimeMs(event.getAccessResult().executionTimeMs())
                .permissionsFiltered(event.wasPermissionFiltered())
                .removedPermissions(event.getRemovedPermissions())
                .interactionId(event.getAccessContext().xFapiInteractionId())
                .customerIpAddress(event.getAccessContext().xFapiCustomerIpAddress())
                .userAgent(event.getAccessContext().xCustomerUserAgent())
                .withinSLA(event.isWithinSLA())
                .auditLevel(event.getComplianceInfo().auditLevel())
                .build();
    }

    /**
     * Detecta acesso de alta frequência
     */
    private boolean detectHighFrequencyAccess(AccountAccessedEvent event) {
        // Implementar lógica de detecção baseada em janela temporal
        return false; // Placeholder
    }

    /**
     * Detecta acesso fora do horário comercial
     */
    private boolean detectOffHoursAccess(AccountAccessedEvent event) {
        var hour = event.getOccurredOn().getHour();
        return hour < 6 || hour > 22; // Fora do horário 06:00-22:00
    }

    /**
     * Detecta padrão incomum de remoção de permissões
     */
    private boolean detectUnusualPermissionPattern(AccountAccessedEvent event) {
        // Implementar lógica baseada em ML ou regras de negócio
        return event.getRemovedPermissions().size() > 5; // Exemplo: muitas permissões removidas
    }

    /**
     * Interfaces dos serviços dependentes
     */
    public interface AuditService {
        void persistAuditRecord(AuditRecord record);
    }

    public interface MetricsService {
        void incrementCounter(String name, String... tags);
        void recordTimer(String name, long duration, String... tags);
        void recordHistogram(String name, double value, String... tags);
    }

    public interface SecurityMonitoringService {
        void reportSuspiciousActivity(String consentId, String organizationId, String ipAddress, String details);
        void reportUnauthorizedIpAccess(String consentId, String ipAddress, String interactionId);
        void escalateSecurityAlert(String consentId, java.util.List<String> securityFlags);
        void reportHighFrequencyAccess(String consentId, String organizationId);
        void reportOffHoursAccess(String consentId, java.time.LocalDateTime accessTime);
    }

    public interface ComplianceReportingService {
        void recordSLACompliance(String endpoint, String organizationId, boolean withinSLA, long responseTime);
        void alertRateLimitNearExhaustion(String organizationId, long remainingRequests);
        void recordPermissionFiltering(String organizationId, java.util.List<String> original,
                                       java.util.List<String> filtered, java.util.List<String> removed);
        void recordAuditLevel(String consentId, AccountAccessedEvent.ComplianceInfo.AuditLevel level);
        void reportUnusualPermissionPattern(String organizationId, java.util.List<String> removedPermissions);
    }

    /**
     * Record para persistência de auditoria
     */
    public record AuditRecord(
            String eventId,
            String eventType,
            String aggregateId,
            java.time.LocalDateTime occurredOn,
            String consentId,
            String organizationId,
            String operation,
            String endpoint,
            boolean success,
            int accountCount,
            long executionTimeMs,
            boolean permissionsFiltered,
            java.util.List<String> removedPermissions,
            String interactionId,
            String customerIpAddress,
            String userAgent,
            boolean withinSLA,
            AccountAccessedEvent.ComplianceInfo.AuditLevel auditLevel
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String eventId;
            private String eventType;
            private String aggregateId;
            private java.time.LocalDateTime occurredOn;
            private String consentId;
            private String organizationId;
            private String operation;
            private String endpoint;
            private boolean success;
            private int accountCount;
            private long executionTimeMs;
            private boolean permissionsFiltered;
            private java.util.List<String> removedPermissions;
            private String interactionId;
            private String customerIpAddress;
            private String userAgent;
            private boolean withinSLA;
            private AccountAccessedEvent.ComplianceInfo.AuditLevel auditLevel;

            public Builder eventId(String eventId) {
                this.eventId = eventId;
                return this;
            }

            public Builder eventType(String eventType) {
                this.eventType = eventType;
                return this;
            }

            public Builder aggregateId(String aggregateId) {
                this.aggregateId = aggregateId;
                return this;
            }

            public Builder occurredOn(java.time.LocalDateTime occurredOn) {
                this.occurredOn = occurredOn;
                return this;
            }

            public Builder consentId(String consentId) {
                this.consentId = consentId;
                return this;
            }

            public Builder organizationId(String organizationId) {
                this.organizationId = organizationId;
                return this;
            }

            public Builder operation(String operation) {
                this.operation = operation;
                return this;
            }

            public Builder endpoint(String endpoint) {
                this.endpoint = endpoint;
                return this;
            }

            public Builder success(boolean success) {
                this.success = success;
                return this;
            }

            public Builder accountCount(int accountCount) {
                this.accountCount = accountCount;
                return this;
            }

            public Builder executionTimeMs(long executionTimeMs) {
                this.executionTimeMs = executionTimeMs;
                return this;
            }

            public Builder permissionsFiltered(boolean permissionsFiltered) {
                this.permissionsFiltered = permissionsFiltered;
                return this;
            }

            public Builder removedPermissions(java.util.List<String> removedPermissions) {
                this.removedPermissions = removedPermissions;
                return this;
            }

            public Builder interactionId(String interactionId) {
                this.interactionId = interactionId;
                return this;
            }

            public Builder customerIpAddress(String customerIpAddress) {
                this.customerIpAddress = customerIpAddress;
                return this;
            }

            public Builder userAgent(String userAgent) {
                this.userAgent = userAgent;
                return this;
            }

            public Builder withinSLA(boolean withinSLA) {
                this.withinSLA = withinSLA;
                return this;
            }

            public Builder auditLevel(AccountAccessedEvent.ComplianceInfo.AuditLevel auditLevel) {
                this.auditLevel = auditLevel;
                return this;
            }

            public AuditRecord build() {
                return new AuditRecord(eventId, eventType, aggregateId, occurredOn, consentId,
                        organizationId, operation, endpoint, success, accountCount, executionTimeMs,
                        permissionsFiltered, removedPermissions, interactionId, customerIpAddress,
                        userAgent, withinSLA, auditLevel);
            }
        }
    }
}