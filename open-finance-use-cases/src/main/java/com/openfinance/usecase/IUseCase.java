package com.openfinance.usecase;

/**
 * Base interface for all use cases in the application
 * Following the single responsibility principle and hexagonal architecture
 *
 * @param <I> Input type for the use case
 * @param <O> Output type for the use case
 */
public interface IUseCase<I, O> {
    /**
     * Executes the use case with the provided input
     *
     * @param input the input data for the use case
     * @return the output data from the use case execution
     * @throws com.openfinance.core.exceptions.DomainException if business rules are violated
     */
    O execute(I input);
}
