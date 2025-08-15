package com.openfinance.usecase.account.retrieve.list;

import com.openfinance.usecase.IUseCase;

public interface IGetAccountsUseCase extends IUseCase<GetAccountsInput, GetAccountsOutput> {

    @Override
    GetAccountsOutput execute(GetAccountsInput input);
}
