package com.openfinance.core.domain.validation;

import java.util.List;

public interface IValidationHandler {

    IValidationHandler append(Error anError);

    IValidationHandler append(IValidationHandler anHandler);

    <T> T validate(Validation<T> aValidation);

    List<Error> getErrors();

    default boolean hasErrors() {
        return getErrors() != null && !getErrors().isEmpty();
    }

    default Error firstError() {
        if (getErrors() != null && !getErrors().isEmpty()) {
            return getErrors().get(0);
        } else {
            return null;
        }
    }

    interface Validation<T> {
        T validate();
    }
}