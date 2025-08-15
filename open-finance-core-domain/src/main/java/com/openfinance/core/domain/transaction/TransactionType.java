package com.openfinance.core.domain.transaction;

/**
 * Enum representing the types of financial transactions
 */
public enum TransactionType {

    /**
     * Transferência Eletrônica Disponível
     */
    TED("TED"),

    /**
     * Documento de Ordem de Crédito
     */
    DOC("DOC"),

    /**
     * PIX instant payment
     */
    PIX("PIX"),

    /**
     * Transfer within the same institution
     */
    TRANSFERENCIA_MESMA_INSTITUICAO("Transferência Mesma Instituição"),

    /**
     * Bank slip payment
     */
    BOLETO("Boleto"),

    /**
     * Collection agreement
     */
    CONVENIO_ARRECADACAO("Convênio Arrecadação"),

    /**
     * Service fee package
     */
    PACOTE_TARIFA_SERVICOS("Pacote Tarifa Serviços"),

    /**
     * Individual service fees
     */
    TARIFA_SERVICOS_AVULSOS("Tarifa Serviços Avulsos"),

    /**
     * Payroll payment
     */
    FOLHA_PAGAMENTO("Folha Pagamento"),

    /**
     * Deposit
     */
    DEPOSITO("Depósito"),

    /**
     * Withdrawal
     */
    SAQUE("Saque"),

    /**
     * Card transaction
     */
    CARTAO("Cartão"),

    /**
     * Overdraft interest charges
     */
    ENCARGOS_JUROS_CHEQUE_ESPECIAL("Encargos Juros Cheque Especial"),

    /**
     * Financial investment yield
     */
    RENDIMENTO_APLIC_FINANCEIRA("Rendimento Aplicação Financeira"),

    /**
     * Salary portability
     */
    PORTABILIDADE_SALARIO("Portabilidade Salário"),

    /**
     * Financial investment redemption
     */
    RESGATE_APLIC_FINANCEIRA("Resgate Aplicação Financeira"),

    /**
     * Credit operation
     */
    OPERACAO_CREDITO("Operação Crédito"),

    /**
     * Other types not covered by the above
     */
    OUTROS("Outros");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns true if the transaction type requires immediate immutability (D0)
     */
    public boolean requiresImmediateImmutability() {
        return switch (this) {
            case TED, PIX, TRANSFERENCIA_MESMA_INSTITUICAO, TARIFA_SERVICOS_AVULSOS, FOLHA_PAGAMENTO -> true;
            default -> false;
        };
    }

    /**
     * Returns true if the transaction type allows immutability on D+1
     */
    public boolean allowsDelayedImmutability() {
        return switch (this) {
            case DOC, BOLETO, CONVENIO_ARRECADACAO, PACOTE_TARIFA_SERVICOS, DEPOSITO, SAQUE,
                 CARTAO, ENCARGOS_JUROS_CHEQUE_ESPECIAL, RENDIMENTO_APLIC_FINANCEIRA,
                 PORTABILIDADE_SALARIO, RESGATE_APLIC_FINANCEIRA, OPERACAO_CREDITO, OUTROS -> true;
            default -> false;
        };
    }
}