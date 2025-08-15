package com.openfinance.core.domain.validation.handler;

import com.openfinance.core.exceptions.DomainException;
import com.openfinance.core.domain.validation.IValidationHandler;
import com.openfinance.core.domain.validation.Error;

import java.util.List;

public class ThrowsValidationHandler implements IValidationHandler {

    @Override
    public IValidationHandler append(final Error anError) {
        throw DomainException.with(anError);
    }

    @Override
    public IValidationHandler append(final IValidationHandler anHandler) {
        throw DomainException.with(anHandler.getErrors());
    }

    @Override
    public <T> T validate(final Validation<T> aValidation) {
        try {
            return aValidation.validate();
        } catch (final Exception ex) {
            throw DomainException.with(new Error(ex.getMessage()));
        }
    }

    @Override
    public List<Error> getErrors() {
        return List.of();
    }
}
