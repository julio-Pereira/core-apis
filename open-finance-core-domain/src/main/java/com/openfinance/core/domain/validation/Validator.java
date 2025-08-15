package com.openfinance.core.domain.validation;

public abstract class Validator {

    private final IValidationHandler handler;

    protected Validator(final IValidationHandler aHandler) {
        this.handler = aHandler;
    }

    public abstract void validate();

    protected IValidationHandler validationHandler() {
        return this.handler;
    }
}