package com.openfinance.usecase.account.mapper;

import com.openfinance.core.domain.account.Account;
import com.openfinance.usecase.account.retrieve.list.AccountOutputDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper para conversão entre entidades de domínio e DTOs do use case.
 *
 * Este mapper é responsável por converter entidades Account do domínio
 * em AccountOutputDto para uso nos casos de uso, mantendo o isolamento
 * entre as camadas conforme a arquitetura hexagonal.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IAccountUseCaseMapper {

    /**
     * Converte uma entidade Account em AccountOutputDto
     */
    @Mapping(target = "accountId", source = "accountId.value")
    @Mapping(target = "type", expression = "java(account.getType().name())")
    @Mapping(target = "subtype", expression = "java(account.getSubtype() != null ? account.getSubtype().name() : null)")
    @Mapping(target = "number", source = "number")
    @Mapping(target = "checkDigit", source = "checkDigit")
    @Mapping(target = "branchCode", source = "branchCode")
    AccountOutputDto toAccountOutputDto(Account account);

    /**
     * Converte uma lista de entidades Account em lista de AccountOutputDto
     */
    List<AccountOutputDto> toAccountOutputDtoList(List<Account> accounts);
}
