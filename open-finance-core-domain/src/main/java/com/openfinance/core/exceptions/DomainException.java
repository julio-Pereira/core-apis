package com.openfinance.core.exceptions;

import java.util.List;
import com.openfinance.core.domain.validation.Error;

public class DomainException extends NoStacktraceException {

    protected final List<Error> errors;

    protected DomainException(final String aMessage, final List<Error> anErrors) {
        super(aMessage);
        this.errors = anErrors;
    }

    public static DomainException with(final Error errors) {
        return new DomainException(errors.message(), List.of(errors));
    }

    public static DomainException with(final List<Error> errors) {
        return new DomainException("", errors);
    }

    public List<Error> getErrors() {
        return errors;
    }
}