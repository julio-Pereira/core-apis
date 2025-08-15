package com.openfinance.core.domain.consent;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum OpenFinancePermission {

    // === ACCOUNTS (CONTAS) ===
    ACCOUNTS_READ("ACCOUNTS_READ", PermissionCategory.ACCOUNTS, ProductGroup.INDIVIDUAL, true),
    ACCOUNTS_BALANCES_READ("ACCOUNTS_BALANCES_READ", PermissionCategory.ACCOUNTS, ProductGroup.INDIVIDUAL, true),
    ACCOUNTS_TRANSACTIONS_READ("ACCOUNTS_TRANSACTIONS_READ", PermissionCategory.ACCOUNTS, ProductGroup.INDIVIDUAL, true),
    ACCOUNTS_OVERDRAFT_LIMITS_READ("ACCOUNTS_OVERDRAFT_LIMITS_READ", PermissionCategory.ACCOUNTS, ProductGroup.INDIVIDUAL, true),

    // === CREDIT CARDS (CARTÕES DE CRÉDITO) ===
    CREDIT_CARDS_ACCOUNTS_READ("CREDIT_CARDS_ACCOUNTS_READ", PermissionCategory.CREDIT_CARDS, ProductGroup.INDIVIDUAL, false),
    CREDIT_CARDS_ACCOUNTS_BILLS_READ("CREDIT_CARDS_ACCOUNTS_BILLS_READ", PermissionCategory.CREDIT_CARDS, ProductGroup.INDIVIDUAL, false),
    CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ("CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ", PermissionCategory.CREDIT_CARDS, ProductGroup.INDIVIDUAL, false),
    CREDIT_CARDS_ACCOUNTS_LIMITS_READ("CREDIT_CARDS_ACCOUNTS_LIMITS_READ", PermissionCategory.CREDIT_CARDS, ProductGroup.INDIVIDUAL, false),
    CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ("CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ", PermissionCategory.CREDIT_CARDS, ProductGroup.INDIVIDUAL, false),

    // === LOANS (EMPRÉSTIMOS) ===
    LOANS_READ("LOANS_READ", PermissionCategory.LOANS, ProductGroup.INDIVIDUAL, false),
    LOANS_WARRANTIES_READ("LOANS_WARRANTIES_READ", PermissionCategory.LOANS, ProductGroup.INDIVIDUAL, false),
    LOANS_SCHEDULED_INSTALMENTS_READ("LOANS_SCHEDULED_INSTALMENTS_READ", PermissionCategory.LOANS, ProductGroup.INDIVIDUAL, false),
    LOANS_PAYMENTS_READ("LOANS_PAYMENTS_READ", PermissionCategory.LOANS, ProductGroup.INDIVIDUAL, false),

    // === FINANCINGS (FINANCIAMENTOS) ===
    FINANCINGS_READ("FINANCINGS_READ", PermissionCategory.FINANCINGS, ProductGroup.INDIVIDUAL, false),
    FINANCINGS_WARRANTIES_READ("FINANCINGS_WARRANTIES_READ", PermissionCategory.FINANCINGS, ProductGroup.INDIVIDUAL, false),
    FINANCINGS_SCHEDULED_INSTALMENTS_READ("FINANCINGS_SCHEDULED_INSTALMENTS_READ", PermissionCategory.FINANCINGS, ProductGroup.INDIVIDUAL, false),
    FINANCINGS_PAYMENTS_READ("FINANCINGS_PAYMENTS_READ", PermissionCategory.FINANCINGS, ProductGroup.INDIVIDUAL, false),

    // === UNARRANGED ACCOUNTS OVERDRAFT (ADIANTAMENTO A DEPOSITANTES) ===
    UNARRANGED_ACCOUNTS_OVERDRAFT_READ("UNARRANGED_ACCOUNTS_OVERDRAFT_READ", PermissionCategory.UNARRANGED_OVERDRAFT, ProductGroup.INDIVIDUAL, false),
    UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ("UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ", PermissionCategory.UNARRANGED_OVERDRAFT, ProductGroup.INDIVIDUAL, false),
    UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ("UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ", PermissionCategory.UNARRANGED_OVERDRAFT, ProductGroup.INDIVIDUAL, false),
    UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ("UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ", PermissionCategory.UNARRANGED_OVERDRAFT, ProductGroup.INDIVIDUAL, false),

    // === INVOICE FINANCINGS (ANTECIPAÇÃO DE RECEBÍVEIS) ===
    INVOICE_FINANCINGS_READ("INVOICE_FINANCINGS_READ", PermissionCategory.INVOICE_FINANCINGS, ProductGroup.INDIVIDUAL, false),
    INVOICE_FINANCINGS_WARRANTIES_READ("INVOICE_FINANCINGS_WARRANTIES_READ", PermissionCategory.INVOICE_FINANCINGS, ProductGroup.INDIVIDUAL, false),
    INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ("INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ", PermissionCategory.INVOICE_FINANCINGS, ProductGroup.INDIVIDUAL, false),
    INVOICE_FINANCINGS_PAYMENTS_READ("INVOICE_FINANCINGS_PAYMENTS_READ", PermissionCategory.INVOICE_FINANCINGS, ProductGroup.INDIVIDUAL, false),

    // === OPERAÇÕES DE CRÉDITO (AGRUPAMENTO) ===
    CREDIT_OPERATIONS_READ("CREDIT_OPERATIONS_READ", PermissionCategory.CREDIT_OPERATIONS, ProductGroup.CREDIT_OPERATIONS_GROUP, false),
    CREDIT_OPERATIONS_WARRANTIES_READ("CREDIT_OPERATIONS_WARRANTIES_READ", PermissionCategory.CREDIT_OPERATIONS, ProductGroup.CREDIT_OPERATIONS_GROUP, false),
    CREDIT_OPERATIONS_SCHEDULED_INSTALMENTS_READ("CREDIT_OPERATIONS_SCHEDULED_INSTALMENTS_READ", PermissionCategory.CREDIT_OPERATIONS, ProductGroup.CREDIT_OPERATIONS_GROUP, false),
    CREDIT_OPERATIONS_PAYMENTS_READ("CREDIT_OPERATIONS_PAYMENTS_READ", PermissionCategory.CREDIT_OPERATIONS, ProductGroup.CREDIT_OPERATIONS_GROUP, false),

    // === INVESTMENTS (INVESTIMENTOS - AGRUPAMENTO) ===
    INVESTMENTS_READ("INVESTMENTS_READ", PermissionCategory.INVESTMENTS, ProductGroup.INVESTMENTS_GROUP, false),
    INVESTMENTS_TRANSACTIONS_READ("INVESTMENTS_TRANSACTIONS_READ", PermissionCategory.INVESTMENTS, ProductGroup.INVESTMENTS_GROUP, false),

    // === EXCHANGE (CÂMBIO - AGRUPAMENTO) ===
    EXCHANGES_READ("EXCHANGES_READ", PermissionCategory.EXCHANGES, ProductGroup.EXCHANGES_GROUP, false),
    EXCHANGES_TRANSACTIONS_READ("EXCHANGES_TRANSACTIONS_READ", PermissionCategory.EXCHANGES, ProductGroup.EXCHANGES_GROUP, false),

    // === RESOURCES (RECURSOS) ===
    RESOURCES_READ("RESOURCES_READ", PermissionCategory.RESOURCES, ProductGroup.INDIVIDUAL, true);

    private final String permission;
    private final PermissionCategory category;
    private final ProductGroup productGroup;
    private final boolean supported;

    OpenFinancePermission(String permission, PermissionCategory category, ProductGroup productGroup, boolean supported) {
        this.permission = permission;
        this.category = category;
        this.productGroup = productGroup;
        this.supported = supported;
    }

    public String getPermission() {
        return permission;
    }

    public PermissionCategory getCategory() {
        return category;
    }

    public ProductGroup getProductGroup() {
        return productGroup;
    }

    public boolean isSupported() {
        return supported;
    }

    /**
     * Verifica se a permissão é de um produto agrupado
     *
     * @return true se pertence a um agrupamento, false caso contrário
     */
    public boolean isGroupedProduct() {
        return productGroup.isGrouped();
    }

    /**
     * Retorna todas as permissões suportadas pela instituição
     *
     * @return Set com permissões suportadas
     */
    public static Set<OpenFinancePermission> getSupportedPermissions() {
        return Arrays.stream(values())
                .filter(OpenFinancePermission::isSupported)
                .collect(Collectors.toSet());
    }

    /**
     * Retorna permissões por categoria
     *
     * @param category Categoria desejada
     * @return Set com permissões da categoria
     */
    public static Set<OpenFinancePermission> getPermissionsByCategory(PermissionCategory category) {
        return Arrays.stream(values())
                .filter(permission -> permission.getCategory() == category)
                .collect(Collectors.toSet());
    }

    /**
     * Retorna permissões por grupo de produto
     *
     * @param productGroup Grupo de produto desejado
     * @return Set com permissões do grupo
     */
    public static Set<OpenFinancePermission> getPermissionsByProductGroup(ProductGroup productGroup) {
        return Arrays.stream(values())
                .filter(permission -> permission.getProductGroup() == productGroup)
                .collect(Collectors.toSet());
    }

    /**
     * Converte string para enum
     *
     * @param permissionString String da permissão
     * @return OpenFinancePermission correspondente ou null se não encontrado
     */
    public static OpenFinancePermission fromString(String permissionString) {
        return Arrays.stream(values())
                .filter(permission -> permission.getPermission().equals(permissionString))
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica se uma string representa uma permissão válida
     *
     * @param permissionString String a ser verificada
     * @return true se válida, false caso contrário
     */
    public static boolean isValidPermission(String permissionString) {
        return fromString(permissionString) != null;
    }

    /**
     * Verifica se uma permissão é funcional (não é apenas de recursos)
     *
     * @return true se é funcional, false caso contrário
     */
    public boolean isFunctional() {
        return this.category != PermissionCategory.RESOURCES;
    }

    @Override
    public String toString() {
        return permission;
    }

    /**
     * Categorias de permissões para organização funcional
     */
    public enum PermissionCategory {
        ACCOUNTS("Contas"),
        CREDIT_CARDS("Cartões de Crédito"),
        LOANS("Empréstimos"),
        FINANCINGS("Financiamentos"),
        UNARRANGED_OVERDRAFT("Adiantamento a Depositantes"),
        INVOICE_FINANCINGS("Antecipação de Recebíveis"),
        CREDIT_OPERATIONS("Operações de Crédito"),
        INVESTMENTS("Investimentos"),
        EXCHANGES("Câmbio"),
        RESOURCES("Recursos");

        private final String description;

        PermissionCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Grupos de produtos para controle de agrupamentos
     */
    public enum ProductGroup {
        INDIVIDUAL("Individual", false),
        CREDIT_OPERATIONS_GROUP("Operações de Crédito", true),
        INVESTMENTS_GROUP("Investimentos", true),
        EXCHANGES_GROUP("Câmbio", true);

        private final String description;
        private final boolean grouped;

        ProductGroup(String description, boolean grouped) {
            this.description = description;
            this.grouped = grouped;
        }

        public String getDescription() {
            return description;
        }

        public boolean isGrouped() {
            return grouped;
        }
    }
}
