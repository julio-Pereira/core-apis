package com.openfinance.usecase.account.retrieve.list;

import com.openfinance.usecase.IUseCase;
import com.openfinance.usecase.utils.AuditLog;
import com.openfinance.usecase.utils.FrequencyCategory;
import com.openfinance.usecase.utils.MonitorPerformance;
import com.openfinance.usecase.utils.MonitorSLA;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetAccountsFacade {

    private final IUseCase<GetAccountsInput, GetAccountsOutput> useCase;

/**
 * Executes the retrieval of the customer's account list.
 *
 * <p>This method automatically applies the following through Spring AOP:
 * <ul>
 *     <li>Audit logging for compliance</li>
 *     <li>Performance monitoring</li>
 *     <li>SLA verification (1500ms for high-frequency endpoints)</li>
 *     <li>Micrometer metrics for observability</li>
 * </ul>
 *
 * @param input Request parameters including consentId, organizationId,
 *              pagination filters, and mandatory FAPI headers.
 * @return A {@link GetAccountsOutput} containing the list of accounts, pagination information,
 *         and the request timestamp.
 */
    @AuditLog(
            operationType = "ACCOUNT_ACCESS"
    )
    @MonitorPerformance(
            operationName = "GET_ACCOUNTS"
    )
    @MonitorSLA(
            endpoint = "/accounts",
            category = FrequencyCategory.HIGH
    )
    public GetAccountsOutput getAccounts(GetAccountsInput input) {
        log.debug("Executing GetAccountsFacade.getAccounts for consentId: {}, organizationId: {}",
                input.consentId(), input.organizationId());

        return useCase.execute(input);
    }
}
