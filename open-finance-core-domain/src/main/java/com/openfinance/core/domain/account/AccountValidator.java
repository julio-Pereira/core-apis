package com.openfinance.core.domain.account;

import com.openfinance.core.domain.validation.IValidationHandler;
import com.openfinance.core.domain.validation.Validator;

public class AccountValidator extends Validator {

    private final Account account;

    public AccountValidator(final Account account, final IValidationHandler handler) {
        super(handler);
        this.account = account;
    }

    public boolean isPrePaidAccount() {
        final var type = this.account.getType();
        return AccountType.CONTA_PAGAMENTO_PRE_PAGA.equals(type);
    }

    @Override
    public void validate() {
        checkPrePaidAccountWithBranchCode();
        checkBranchCodeIsPresentForNonPrePaidAccount();
    }

    private void checkPrePaidAccountWithBranchCode() {
        if (isPrePaidAccount() && this.account.getBranchCode() != null) {
            throw new IllegalArgumentException("Pre-paid accounts cannot have a branch code");
        }
    }

    private void checkBranchCodeIsPresentForNonPrePaidAccount() {
        if (!isPrePaidAccount() && this.account.getBranchCode() == null) {
            throw new IllegalArgumentException("Non pre-paid accounts must have a branch code");
        }
    }
}
