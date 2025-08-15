package com.openfinance.usecase.account.retrieve.list;

import com.openfinance.core.domain.account.Account;
import com.openfinance.core.domain.account.AccountSubType;
import com.openfinance.core.domain.account.AccountType;
import com.openfinance.core.domain.account.AccountId;
import com.openfinance.core.domain.valueobjects.BranchCode;
import com.openfinance.usecase.account.mapper.IAccountUseCaseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static com.openfinance.core.domain.account.AccountSubType.INDIVIDUAL;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountUseCaseMapperTest {
    private IAccountUseCaseMapper mapper;
    private Account mockAccount;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(IAccountUseCaseMapper.class);


        mockAccount = Account.builder()
                .accountId(AccountId.unique())
                .type(AccountType.CONTA_DEPOSITO_A_VISTA)
                .subtype(INDIVIDUAL)
                .number("12345")
                .checkDigit("6")
                .branchCode(BranchCode.of("0001"))
                .build();
    }

    @Test
    @DisplayName("Deve converter Account para AccountOutputDto corretamente")
    void shouldMapAccountToAccountOutputDtoCorrectly() {
        // When
        AccountOutputDto result = mapper.toAccountOutputDto(mockAccount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accountId()).isEqualTo("92792126019929279212650822221989319252576");
        assertThat(result.type()).isEqualTo("CONTA_DEPOSITO_A_VISTA");
        assertThat(result.subtype()).isEqualTo("INDIVIDUAL");
        assertThat(result.number()).isEqualTo("12345");
        assertThat(result.checkDigit()).isEqualTo("6");
        assertThat(result.branchCode()).isEqualTo("0001");
    }



    @Test
    @DisplayName("Deve converter lista de Accounts para lista de AccountOutputDtos")
    void shouldMapAccountListToAccountOutputDtoList() {
        // Given
        Account secondAccount = Account.builder()
                .accountId(AccountId.unique())
                .type(AccountType.CONTA_POUPANCA)
                .subtype(AccountSubType.CONJUNTA_SIMPLES)
                .number("67890")
                .checkDigit("1")
                .branchCode(BranchCode.of("0002"))
                .build();

        List<Account> accounts = List.of(mockAccount, secondAccount);

        // When
        List<AccountOutputDto> result = mapper.toAccountOutputDtoList(accounts);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // Verificar primeira conta
        AccountOutputDto firstOutput = result.get(0);
        assertThat(firstOutput.accountId()).isEqualTo("92792126019929279212650822221989319252576");
        assertThat(firstOutput.type()).isEqualTo("CONTA_DEPOSITO_A_VISTA");
        assertThat(firstOutput.subtype()).isEqualTo("INDIVIDUAL");
        assertThat(firstOutput.nickname()).isEqualTo("Minha Conta Corrente");

        // Verificar segunda conta
        AccountOutputDto secondOutput = result.get(1);
        assertThat(secondOutput.accountId()).isEqualTo("98765432109876543210987654321098765432109");
        assertThat(secondOutput.type()).isEqualTo("CONTA_POUPANCA");
        assertThat(secondOutput.subtype()).isEqualTo("CONJUNTA");
        assertThat(secondOutput.nickname()).isEqualTo("Poupança Familiar");
    }

    @Test
    @DisplayName("Deve tratar Account com subtype nulo")
    void shouldHandleAccountWithNullSubtype() {
        // Given
        Account accountWithoutSubtype = Account.builder()
                .accountId(AccountId.unique())
                .type(AccountType.CONTA_PAGAMENTO_PRE_PAGA)
                .subtype(null)
                .number("11111")
                .checkDigit("1")
                .branchCode(BranchCode.of("0003"))
                .build();

        // When
        AccountOutputDto result = mapper.toAccountOutputDto(accountWithoutSubtype);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accountId()).isEqualTo("11111111111111111111111111111111111111111");
        assertThat(result.type()).isEqualTo("CONTA_PAGAMENTO_PRE_PAGA");
        assertThat(result.subtype()).isNull();
        assertThat(result.nickname()).isEqualTo("Cartão Pré-pago");
    }


    @Test
    @DisplayName("Deve tratar todos os tipos de conta corretamente")
    void shouldHandleAllAccountTypesCorrectly() {
        // Given
        Account contaCorrente = mockAccount; // já é CONTA_DEPOSITO_A_VISTA

        Account contaPoupanca = Account.builder()
                .accountId(AccountId.unique())
                .type(AccountType.CONTA_POUPANCA)
                .subtype(AccountSubType.INDIVIDUAL)
                .number("44444")
                .checkDigit("4")
                .branchCode(BranchCode.of("0006"))
                .build();

        Account contaPrePaga = Account.builder()
                .accountId(AccountId.unique())
                .type(AccountType.CONTA_PAGAMENTO_PRE_PAGA)
                .subtype(AccountSubType.INDIVIDUAL)
                .number("55555")
                .checkDigit("5")
                .branchCode(BranchCode.of("0007"))
                .build();

        // When
        AccountOutputDto correnteDto = mapper.toAccountOutputDto(contaCorrente);
        AccountOutputDto poupancaDto = mapper.toAccountOutputDto(contaPoupanca);
        AccountOutputDto prePagaDto = mapper.toAccountOutputDto(contaPrePaga);

        // Then
        assertThat(correnteDto.type()).isEqualTo("CONTA_DEPOSITO_A_VISTA");
        assertThat(poupancaDto.type()).isEqualTo("CONTA_POUPANCA");
        assertThat(prePagaDto.type()).isEqualTo("CONTA_PAGAMENTO_PRE_PAGA");
    }

    @Test
    @DisplayName("Deve tratar todos os subtipos de conta corretamente")
    void shouldHandleAllAccountSubtypesCorrectly() {
        // Given
        Account contaIndividual = mockAccount; // já é INDIVIDUAL

        Account contaConjunta = Account.builder()
                .accountId(AccountId.unique())
                .type(AccountType.CONTA_DEPOSITO_A_VISTA)
                .subtype(AccountSubType.CONJUNTA_SIMPLES)
                .number("66666")
                .checkDigit("6")
                .branchCode(BranchCode.of("0008"))
                .build();

        // When
        AccountOutputDto individualDto = mapper.toAccountOutputDto(contaIndividual);
        AccountOutputDto conjuntaDto = mapper.toAccountOutputDto(contaConjunta);

        // Then
        assertThat(individualDto.subtype()).isEqualTo("INDIVIDUAL");
        assertThat(conjuntaDto.subtype()).isEqualTo("CONJUNTA");
    }

    @Test
    @DisplayName("Deve preservar integridade dos dados durante conversão")
    void shouldPreserveDataIntegrityDuringMapping() {
        // When
        AccountOutputDto result = mapper.toAccountOutputDto(mockAccount);

        // Then - verificar que todos os campos foram mapeados corretamente
        assertThat(result.accountId()).isNotEmpty();
        assertThat(result.type()).isNotEmpty();
        assertThat(result.subtype()).isNotEmpty();
        assertThat(result.number()).isNotEmpty();
        assertThat(result.checkDigit()).isNotEmpty();
        assertThat(result.branchCode()).isNotEmpty();

        // Verificar que não há perda de informação
        assertThat(result.accountId()).hasSize(mockAccount.getId().getValue().length());
    }
}
