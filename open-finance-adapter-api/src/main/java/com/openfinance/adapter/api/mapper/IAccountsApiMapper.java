package com.openfinance.adapter.api.mapper;

import com.openfinance.adapter.api.dto.response.AccountDataDto;
import com.openfinance.adapter.api.dto.response.CompanyDto;
import com.openfinance.adapter.api.dto.response.LinksDto;
import com.openfinance.adapter.api.dto.response.MetaDto;
import com.openfinance.adapter.api.dto.response.ResponseAccountListDto;
import com.openfinance.usecase.account.retrieve.list.AccountOutputDto;
import com.openfinance.usecase.account.retrieve.list.GetAccountsOutput;
import com.openfinance.usecase.pagination.PaginationInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mapper interface for converting between Use Case output DTOs and API response DTOs.
 *
 * This mapper works ONLY with Use Case DTOs, maintaining proper separation
 * between layers according to Hexagonal Architecture principles.
 *
 * IMPORTANT: This mapper should NOT import domain entities directly.
 * All conversions should go through Use Case output DTOs.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IAccountsApiMapper {

    /**
     * Maps GetAccountsOutput (Use Case DTO) to ResponseAccountListDto (API DTO)
     */
    @Mapping(target = "data", source = "accounts")
    @Mapping(target = "links", source = "paginationInfo")
    @Mapping(target = "meta", source = ".")
    ResponseAccountListDto toResponseAccountListDto(GetAccountsOutput getAccountsOutput);

    /**
     * Maps AccountOutputDto (Use Case DTO) to AccountDataDto (API DTO)
     */
    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "subtype", source = "subtype")
    @Mapping(target = "nickname", source = "nickname")
    @Mapping(target = "company", source = "company")
    @Mapping(target = "number", source = "number")
    @Mapping(target = "checkDigit", source = "checkDigit")
    @Mapping(target = "branchCode", source = "branchCode")
    @Mapping(target = "branchCheckDigit", source = "branchCheckDigit")
    AccountDataDto toAccountDataDto(AccountOutputDto accountOutputDto);

    /**
     * Maps list of AccountOutputDto to list of AccountDataDto
     */
    List<AccountDataDto> toAccountDataDtoList(List<AccountOutputDto> accounts);

    /**
     * Maps CompanyOutputDto (Use Case DTO) to CompanyDto (API DTO)
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "cnpjNumber", source = "cnpjNumber")
    CompanyDto toCompanyDto(CompanyOutputDto companyOutputDto);

    /**
     * Maps PaginationInfo to LinksDto
     */
    @Mapping(target = "self", source = "selfLink")
    @Mapping(target = "first", source = "firstLink")
    @Mapping(target = "prev", source = "prevLink")
    @Mapping(target = "next", source = "nextLink")
    @Mapping(target = "last", source = "lastLink")
    LinksDto toLinksDto(PaginationInfo paginationInfo);

    /**
     * Maps GetAccountsOutput to MetaDto
     */
    @Mapping(target = "totalRecords", source = "paginationInfo.totalRecords")
    @Mapping(target = "totalPages", source = "paginationInfo.totalPages")
    @Mapping(target = "requestDateTime", source = "requestDateTime")
    MetaDto toMetaDto(GetAccountsOutput getAccountsOutput);

    /**
     * Creates MetaDto with just requestDateTime for error responses
     */
    default MetaDto toErrorMetaDto(LocalDateTime requestDateTime) {
        return new MetaDto(null, null, requestDateTime);
    }
}