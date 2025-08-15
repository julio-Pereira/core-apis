package com.openfinance.core.events;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;

/**
 * Interface base para todos os eventos de domínio.
 *
 * Define o contrato padrão para eventos seguindo princípios de
 * Domain-Driven Design (DDD) e Event Sourcing.
 *
 * Características:
 * - Imutabilidade garantida
 * - Metadados padronizados
 * - Serialização JSON configurada
 * - Rastreabilidade completa
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public interface DomainEvent {

    /**
     * Identificador único do evento
     *
     * @return UUID do evento
     */
    String getEventId();

    /**
     * Timestamp de quando o evento ocorreu
     *
     * @return Data e hora da ocorrência
     */
    LocalDateTime getOccurredOn();

    /**
     * Identificador do agregado que gerou o evento
     *
     * @return ID do agregado
     */
    String getAggregateId();

    /**
     * Tipo/nome do evento para classificação
     *
     * @return Nome do tipo do evento
     */
    String getEventType();

    /**
     * Versão do evento para evolução de schema
     *
     * @return Versão do evento (default: 1)
     */
    default int getEventVersion() {
        return 1;
    }

    /**
     * Contexto adicional do evento
     *
     * @return Informações contextuais opcionais
     */
    default String getEventContext() {
        return null;
    }

    /**
     * Verifica se o evento é crítico para auditoria
     *
     * @return true se é crítico, false caso contrário
     */
    default boolean isCritical() {
        return false;
    }

    /**
     * Retorna representação estruturada do evento para logs
     *
     * @return String formatada para logging
     */
    default String toLogString() {
        return String.format("DomainEvent{type=%s, id=%s, aggregateId=%s, occurredOn=%s}",
                getEventType(), getEventId(), getAggregateId(), getOccurredOn());
    }
}
