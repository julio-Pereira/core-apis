# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.4 application for Open Finance Account APIs using Java 17. The project follows standard Maven directory structure with minimal dependencies (spring-boot-starter and spring-boot-starter-test).

## Build and Development Commands

### Maven Commands
```bash
# Build the project
./mvnw clean compile

# Run tests
./mvnw test

# Run a single test class
./mvnw -Dtest=CoreApisApplicationTests test

# Package the application
./mvnw clean package

# Run the application
./mvnw spring-boot:run
```

## Project Rules

- Utilizar a arquitetura hexagonal.

- Adapter, domains, use-cases devem estar em seu próprio módulo (ao invés de estarem dentro de uma pasta main, crie seus próprios módulos. Exemplos de módulos: open-finance-adapter-api, open-finance-core-domain, open-finance-use-cases)

- Utilizar as seguintes tecnologias de desenvolvimento: Java 17, Spring Webflux, Spring Boot, MapStructs e maven.

- Toda interface Java deve iniciar com o prefixo "I" para facilitar a identificação de interfaces na aplicação. Exemplo: IAccountPort.java

- Utilizar as seguinte stecnologias de testes unitários: JUnit, Mockito.

- Utilizar o Springdoc-openapi-ui para documentar os endpoints, utilizando o Swagger UI.

- Utilizar docker e docker compose para ambiente de desenvolvimento (Kubernetes será usado para ambiente prod)

- Sempre desenvolver o código respeitando os princípios dos 12 fatores.

- Sempre desenvolver o código respeitando os princípios SOLID.

- Utilizar design patterns onde for aplicável.

- Utilize record para lidar com DTOs.

- Utilizar o Spring Pageable para paginação e sorting.

- Sempre trabalhar em pequenas tasks para então ir juntando os pedaços até conquistar o objetivo final.

- Utilizar JWT, OAuth2 para lidar com o consentId.

- Utilizar ferramentas do Java/Spring  que lidam com web client para conectar/enviar dados da request para outro serviços que nos encaminha os dados de volta como response.

- Utilizar Global Exception Handler do Spring para lidar com todos os tipos de http que lidam com exceptions como 4xx e 5xx.

- Seguir estritamente os schemas dos endpoints fornecidos.

- Essa API de Accounts só possuirá ações de consulta, ou seja, não é de responsabilidade dessa aplicação realizar ações de atualização, remoção ou inserção de contas ou transações, todos esses dados virão de uma outra API, ou seja, essa aplicação receberá uma requisição, enviará a requisição para a outra API e irá tratar os dados conforme requisições do open finance e enviar os dados tratados para a API que enviou a requisição.

- Para operações que exigem alguma persistência de dados, iremos usar o Spring Mongodb.

## Important Notes

- Package name uses underscores (`com.openfinance.core_apis`) instead of hyphens due to Java naming conventions
- Currently a minimal Spring Boot starter project with basic structure in place
- Uses Spring Boot Maven Plugin for packaging and running

### Open API containing API Accounts rules

openapi: 3.0.0
info:
title: API Accounts - Open Finance Brasil
description: |
API de contas de depósito à vista, contas de poupança e contas pré-pagas do Open Finance Brasil – Fase 2.
API que retorna informações de contas de depósito à vista, contas de poupança e contas de pagamento pré-pagas mantidas nas instituições transmissoras por seus clientes,
incluindo dados de identificação da conta, saldos, limites e transações.\
Não possui segregação entre pessoa natural e pessoa jurídica.\
Requer consentimento do cliente para todos os `endpoints`.

    # Orientações
    A `Role`  do diretório de participantes relacionada à presente API é a `DADOS`.\
    Para todos os `endpoints` desta API é previsto o envio de um `token` através do header `Authorization`.\
    Este token deverá estar relacionado ao consentimento (`consentId`) mantido na instituição transmissora dos dados, o qual permitirá a pesquisa e retorno, na API em questão, dos 
    dados relacionados ao `consentId` específico relacionado.\
    Os dados serão devolvidos na consulta desde que o `consentId` relacionado corresponda a um consentimento válido e com o status `AUTHORISED`.\
    É também necessário que o recurso em questão (conta, contrato, etc) esteja disponível na instituição transmissora (ou seja, sem boqueios de qualquer natureza e com todas as autorizações/consentimentos já autorizados).\
    Além disso as `permissions` necessárias deverão ter sido solicitadas quando da criação do consentimento relacionado (`consentId`).\
    Relacionamos a seguir as `permissions` necessárias para a consulta de dados em cada `endpoint` da presente API.

    ## Permissions necessárias para a API Accounts

    Para cada um dos paths desta API, além dos escopos (`scopes`) indicados existem `permissions` que deverão ser observadas:

    ### `/accounts`
      - permissions:
        - GET: **ACCOUNTS_READ**
    ### `/accounts/{accountId}`
      - permissions:
        - GET: **ACCOUNTS_READ**
    ### `/accounts/{accountId}/balances`
      - permissions:
        - GET: **ACCOUNTS_BALANCES_READ**
    ### `/accounts/{accountId}/transactions`
      - permissions:
        - GET: **ACCOUNTS_TRANSACTIONS_READ**
    ### `/accounts/{accountId}/transactions-current`
      - permissions:
        - GET: **ACCOUNTS_TRANSACTIONS_READ**
    ### `/accounts/{accountId}/overdraft-limits`
      - permissions:
        - GET: **ACCOUNTS_OVERDRAFT_LIMITS_READ**

    ## Data de imutabilidade por tipo de transação​
    O identificador de transações de contas é de envio obrigatório no Open Finance Brasil. De acordo com o tipo da transação deve haver o envio de um identificador único, estável e imutável em D0 ou D+1, conforme tabela abaixo
    ```
    |---------------------------------------|-------------------------|-----------------------|
    | Tipo de Transação                     | Data da Obrigatoriedade | Data da Imutabilidade |
    |---------------------------------------|-------------------------|-----------------------|
    | TED                                   | DO                      | DO                    |
    |---------------------------------------|-------------------------|-----------------------|
    | PIX                                   | DO                      | DO                    |
    |---------------------------------------|-------------------------|-----------------------|
    | TRANSFERENCIA MESMA INSTITUIÇÃO (TEF) | DO                      | DO                    |
    |---------------------------------------|-------------------------|-----------------------|
    | TARIFA SERVIÇOS AVULSOS               | DO                      | DO                    |
    |---------------------------------------|-------------------------|-----------------------|
    | FOLHA DE PAGAMENTO                    | DO                      | DO                    |
    |---------------------------------------|-------------------------|-----------------------|
    | DOC                                   | DO                      | D+1                   |
    |---------------------------------------|-------------------------|-----------------------|
    | BOLETO                                | DO                      | D+1                   |
    |---------------------------------------|-------------------------|-----------------------|
    | CONVÊNIO ARRECADAÇÃO                  | DO                      | D+1                   |
    |---------------------------------------|-------------------------|-----------------------|
    | PACOTE TARIFA SERVIÇOS                | DO                      | D+1                   |
    |---------------------------------------|-------------------------|-----------------------|
    | DEPÓSITO                              | DO                      | D+1                   |
    |---------------------------------------|-------------------------|-----------------------|
    | SAQUE                                 | DO                      | D+1                   |
    |---------------------------------------|-------------------------|-----------------------|
    | CARTÃO                                | DO                      | D+1                   |
    |---------------------------------------|-------------------------|-----------------------|
    | ENCARGOS JUROS CHEQUE ESPECIAL        | DO                      | D+1                   |
    |---------------------------------------|-------------------------|-----------------------|
    | RENDIMENTO APLICAÇÃO FINANCEIRA       | DO                      | D+1                   |
    |---------------------------------------|-------------------------|-----------------------|
    | PORTABILIDADE SALÁRIO                 | DO                      | D+1                   |
    |---------------------------------------|-------------------------|-----------------------|
    | RESGATE APLICAÇÃO FINANCEIRA          | DO                      | D+1                   |
    |---------------------------------------|-------------------------|-----------------------|
    | OPERAÇÃO DE CRÉDITO                   | DO                      | D+1                   |
    |---------------------------------------|-------------------------|-----------------------|
    | OUTROS                                | DO                      | D+1                   |
    |---------------------------------------|-------------------------|-----------------------|
    ```

    Para consultar as regras aplicáveis ao comportamento do transacionID de acordo com o status da transação, consultar a página [Orientações - Contas](https://openfinancebrasil.atlassian.net/wiki/spaces/OF/pages/193658890)
version: 2.4.2
license:
name: Apache 2.0
url: 'https://www.apache.org/licenses/LICENSE-2.0'
contact:
name: Governança do Open Finance Brasil – Especificações
email: gt-interfaces@openbankingbr.org
url: 'https://openbanking-brasil.github.io/areadesenvolvedor/'
servers:
- url: 'https://api.banco.com.br/open-banking/accounts/v2'
  description: Servidor de Produção
- url: 'https://apih.banco.com.br/open-banking/accounts/v2'
  description: Servidor de Homologação
  tags:
- name: Accounts
  description: Operações para listagem das informações da Conta do Cliente
  paths:
  /accounts:
  get:
  tags:
  - Accounts
  summary: Obtém a lista de contas consentidas pelo cliente.
  operationId: accountsGetAccounts
  description: 'Método para obter a lista de contas depósito à vista, poupança e pagamento pré-pagas mantidas pelo cliente na instituição transmissora e para as quais ele tenha fornecido consentimento.'
  parameters:
  - $ref: '#/components/parameters/Authorization'
  - $ref: '#/components/parameters/xFapiAuthDate'
  - $ref: '#/components/parameters/xFapiCustomerIpAddress'
  - $ref: '#/components/parameters/xFapiInteractionId'
  - $ref: '#/components/parameters/xCustomerUserAgent'
  - $ref: '#/components/parameters/page'
  - $ref: '#/components/parameters/pageSize'
  - $ref: '#/components/parameters/accountType'
  - $ref: '#/components/parameters/pagination-key'
  responses:
  '200':
  $ref: '#/components/responses/OKResponseAccountList'
  '400':
  $ref: '#/components/responses/BadRequestWithAdditionalProperties'
  '401':
  $ref: '#/components/responses/UnauthorizedWithAdditionalProperties'
  '403':
  $ref: '#/components/responses/ForbiddenWithAdditionalProperties'
  '404':
  $ref: '#/components/responses/NotFoundWithAdditionalProperties'
  '405':
  $ref: '#/components/responses/MethodNotAllowedWithAdditionalProperties'
  '406':
  $ref: '#/components/responses/NotAcceptableWithAdditionalProperties'
  '422':
  $ref: '#/components/responses/UnprocessableEntityWithAdditionalProperties'
  '423':
  $ref: '#/components/responses/LockedWithAdditionalProperties'
  '429':
  $ref: '#/components/responses/TooManyRequestsWithAdditionalProperties'
  '500':
  $ref: '#/components/responses/InternalServerErrorWithAdditionalProperties'
  '504':
  $ref: '#/components/responses/GatewayTimeoutWithAdditionalProperties'
  '529':
  $ref: '#/components/responses/SiteIsOverloadedWithAdditionalProperties'
  default:
  $ref: '#/components/responses/DefaultWithAdditionalProperties'
  security:
  - OpenId:
  - openid
  OAuth2Security:
  - 'consent:consentId'
  - accounts
  '/accounts/{accountId}':
  get:
  tags:
  - Accounts
  summary: Obtém os dados de identificação da conta identificada por accountId.
  description: 'Método para obter os dados de identificação da conta de depósito à vista, poupança ou pagamento pré-paga identificada por accountId mantida pelo cliente na instituição transmissora.'
  operationId: accountsGetAccountsAccountId
  parameters:
  - $ref: '#/components/parameters/Authorization'
  - $ref: '#/components/parameters/xFapiAuthDate'
  - $ref: '#/components/parameters/xFapiCustomerIpAddress'
  - $ref: '#/components/parameters/xFapiInteractionId'
  - $ref: '#/components/parameters/xCustomerUserAgent'
  - $ref: '#/components/parameters/accountId'
  responses:
  '200':
  $ref: '#/components/responses/OKResponseAccountIdentification'
  '400':
  $ref: '#/components/responses/BadRequestWithAdditionalProperties'
  '401':
  $ref: '#/components/responses/UnauthorizedWithAdditionalProperties'
  '403':
  $ref: '#/components/responses/ForbiddenWithAdditionalProperties'
  '404':
  $ref: '#/components/responses/NotFoundWithAdditionalProperties'
  '405':
  $ref: '#/components/responses/MethodNotAllowedWithAdditionalProperties'
  '406':
  $ref: '#/components/responses/NotAcceptableWithAdditionalProperties'
  '422':
  $ref: '#/components/responses/UnprocessableEntityWithAdditionalProperties'
  '423':
  $ref: '#/components/responses/LockedWithAdditionalProperties'
  '429':
  $ref: '#/components/responses/TooManyRequestsWithAdditionalProperties'
  '500':
  $ref: '#/components/responses/InternalServerErrorWithAdditionalProperties'
  '504':
  $ref: '#/components/responses/GatewayTimeoutWithAdditionalProperties'
  '529':
  $ref: '#/components/responses/SiteIsOverloadedWithAdditionalProperties'
  default:
  $ref: '#/components/responses/DefaultWithAdditionalProperties'
  security:
  - OpenId:
  - openid
  OAuth2Security:
  - 'consent:consentId'
  - accounts
  '/accounts/{accountId}/balances':
  get:
  tags:
  - Accounts
  summary: Obtém os saldos da conta identificada por accountId.
  operationId: accountsGetAccountsAccountIdBalances
  description: 'Método para obter os saldos da conta de depósito à vista, poupança ou pagamento pré-paga identificada por accountId mantida pelo cliente na instituição transmissora.'
  parameters:
  - $ref: '#/components/parameters/Authorization'
  - $ref: '#/components/parameters/xFapiAuthDate'
  - $ref: '#/components/parameters/xFapiCustomerIpAddress'
  - $ref: '#/components/parameters/xFapiInteractionId'
  - $ref: '#/components/parameters/xCustomerUserAgent'
  - $ref: '#/components/parameters/accountId'
  responses:
  '200':
  $ref: '#/components/responses/OKResponseAccountBalances'
  '400':
  $ref: '#/components/responses/BadRequestWithAdditionalProperties'
  '401':
  $ref: '#/components/responses/UnauthorizedWithAdditionalProperties'
  '403':
  $ref: '#/components/responses/ForbiddenWithAdditionalProperties'
  '404':
  $ref: '#/components/responses/NotFoundWithAdditionalProperties'
  '405':
  $ref: '#/components/responses/MethodNotAllowedWithAdditionalProperties'
  '406':
  $ref: '#/components/responses/NotAcceptableWithAdditionalProperties'
  '422':
  $ref: '#/components/responses/UnprocessableEntityWithAdditionalProperties'
  '423':
  $ref: '#/components/responses/LockedWithAdditionalProperties'
  '429':
  $ref: '#/components/responses/TooManyRequestsWithAdditionalProperties'
  '500':
  $ref: '#/components/responses/InternalServerErrorWithAdditionalProperties'
  '504':
  $ref: '#/components/responses/GatewayTimeoutWithAdditionalProperties'
  '529':
  $ref: '#/components/responses/SiteIsOverloadedWithAdditionalProperties'
  default:
  $ref: '#/components/responses/DefaultWithAdditionalProperties'
  security:
  - OpenId:
  - openid
  OAuth2Security:
  - 'consent:consentId'
  - accounts
  '/accounts/{accountId}/transactions':
  get:
  tags:
  - Accounts
  summary: Obtém a lista de transações da conta identificada por accountId.
  operationId: accountsGetAccountsAccountIdTransactions
  description: Método para obter a lista de transações da conta de depósito à vista, poupança ou pagamento pré-paga identificada por accountId mantida pelo cliente na instituição transmissora. É permitida uma consulta máxima que se estenda em 12 meses no passado mais 12 meses no futuro.
  parameters:
  - $ref: '#/components/parameters/Authorization'
  - $ref: '#/components/parameters/xFapiAuthDate'
  - $ref: '#/components/parameters/xFapiCustomerIpAddress'
  - $ref: '#/components/parameters/xFapiInteractionId'
  - $ref: '#/components/parameters/xCustomerUserAgent'
  - $ref: '#/components/parameters/accountId'
  - $ref: '#/components/parameters/page'
  - $ref: '#/components/parameters/pageSize'
  - $ref: '#/components/parameters/fromBookingDate'
  - $ref: '#/components/parameters/toBookingDate'
  - $ref: '#/components/parameters/creditDebitIndicator'
  - $ref: '#/components/parameters/pagination-key'
  responses:
  '200':
  $ref: '#/components/responses/OKResponseAccountTransactions'
  '400':
  $ref: '#/components/responses/BadRequest'
  '401':
  $ref: '#/components/responses/Unauthorized'
  '403':
  $ref: '#/components/responses/Forbidden'
  '404':
  $ref: '#/components/responses/NotFound'
  '405':
  $ref: '#/components/responses/MethodNotAllowed'
  '406':
  $ref: '#/components/responses/NotAcceptable'
  '422':
  $ref: '#/components/responses/UnprocessableEntity'
  '423':
  $ref: '#/components/responses/Locked'
  '429':
  $ref: '#/components/responses/TooManyRequests'
  '500':
  $ref: '#/components/responses/InternalServerError'
  '504':
  $ref: '#/components/responses/GatewayTimeout'
  '529':
  $ref: '#/components/responses/SiteIsOverloaded'
  default:
  $ref: '#/components/responses/Default'
  security:
  - OpenId:
  - openid
  OAuth2Security:
  - 'consent:consentId'
  - accounts
  '/accounts/{accountId}/transactions-current':
  get:
  tags:
  - Accounts
  summary: Obtém a lista de transações recentes (últimos 7 dias) da conta identificada por accountId.
  operationId: accountsGetAccountsAccountIdTransactionsCurrent
  description: Método para obter a lista de transações da conta de depósito à vista, poupança ou pagamento pré-paga identificada por accountId mantida pelo cliente na instituição transmissora. É permitida uma consulta máxima que se estenda em 7 dias no passado mais 12 meses no futuro.
  parameters:
  - $ref: '#/components/parameters/Authorization'
  - $ref: '#/components/parameters/xFapiAuthDate'
  - $ref: '#/components/parameters/xFapiCustomerIpAddress'
  - $ref: '#/components/parameters/xFapiInteractionId'
  - $ref: '#/components/parameters/xCustomerUserAgent'
  - $ref: '#/components/parameters/accountId'
  - $ref: '#/components/parameters/page'
  - $ref: '#/components/parameters/pageSize'
  - $ref: '#/components/parameters/fromBookingDateMaxLimited'
  - $ref: '#/components/parameters/toBookingDateMaxLimited'
  - $ref: '#/components/parameters/creditDebitIndicator'
  - $ref: '#/components/parameters/pagination-key'
  responses:
  '200':
  $ref: '#/components/responses/OKResponseAccountTransactions'
  '400':
  $ref: '#/components/responses/BadRequest'
  '401':
  $ref: '#/components/responses/Unauthorized'
  '403':
  $ref: '#/components/responses/Forbidden'
  '404':
  $ref: '#/components/responses/NotFound'
  '405':
  $ref: '#/components/responses/MethodNotAllowed'
  '406':
  $ref: '#/components/responses/NotAcceptable'
  '422':
  $ref: '#/components/responses/UnprocessableEntity'
  '423':
  $ref: '#/components/responses/Locked'
  '429':
  $ref: '#/components/responses/TooManyRequests'
  '500':
  $ref: '#/components/responses/InternalServerError'
  '504':
  $ref: '#/components/responses/GatewayTimeout'
  '529':
  $ref: '#/components/responses/SiteIsOverloaded'
  default:
  $ref: '#/components/responses/Default'
  security:
  - OpenId:
  - openid
  OAuth2Security:
  - 'consent:consentId'
  - accounts
  '/accounts/{accountId}/overdraft-limits':
  get:
  tags:
  - Accounts
  summary: Obtém os limites da conta identificada por accountId.
  operationId: accountsGetAccountsAccountIdOverdraftLimits
  description: Método para obter os limites da conta de depósito à vista, poupança ou pagamento pré-paga identificada por accountId mantida pelo cliente na instituição transmissora. Para as instituições financeiras transmissoras que possuam contas sem limites associados devem retornar HTTP Status 200 com o objeto “data” vazio, sem nenhum atributo interno.
  parameters:
  - $ref: '#/components/parameters/Authorization'
  - $ref: '#/components/parameters/xFapiAuthDate'
  - $ref: '#/components/parameters/xFapiCustomerIpAddress'
  - $ref: '#/components/parameters/xFapiInteractionId'
  - $ref: '#/components/parameters/xCustomerUserAgent'
  - $ref: '#/components/parameters/accountId'
  responses:
  '200':
  $ref: '#/components/responses/OKResponseAccountOverdraftLimits'
  '400':
  $ref: '#/components/responses/BadRequestWithAdditionalProperties'
  '401':
  $ref: '#/components/responses/UnauthorizedWithAdditionalProperties'
  '403':
  $ref: '#/components/responses/ForbiddenWithAdditionalProperties'
  '404':
  $ref: '#/components/responses/NotFoundWithAdditionalProperties'
  '405':
  $ref: '#/components/responses/MethodNotAllowedWithAdditionalProperties'
  '406':
  $ref: '#/components/responses/NotAcceptableWithAdditionalProperties'
  '422':
  $ref: '#/components/responses/UnprocessableEntityWithAdditionalProperties'
  '423':
  $ref: '#/components/responses/LockedWithAdditionalProperties'
  '429':
  $ref: '#/components/responses/TooManyRequestsWithAdditionalProperties'
  '500':
  $ref: '#/components/responses/InternalServerErrorWithAdditionalProperties'
  '504':
  $ref: '#/components/responses/GatewayTimeoutWithAdditionalProperties'
  '529':
  $ref: '#/components/responses/SiteIsOverloadedWithAdditionalProperties'
  default:
  $ref: '#/components/responses/DefaultWithAdditionalProperties'
  security:
  - OpenId:
  - openid
  OAuth2Security:
  - 'consent:consentId'
  - accounts
  components:
  schemas:
  AccountBalancesDataAutomaticallyInvestedAmount:
  type: object
  description: Saldo disponível com aplicação automática - corresponde a soma do saldo disponível acrescido do valor obtido a partir da aplicação automática. Expresso em valor monetário com no mínimo 2 casas e no máximo 4 casas decimais.
  required:
  - amount
  - currency
  properties:
  amount:
  type: string
  format: double
  pattern: '^-?\d{1,15}\.\d{2,4}$'
  maxLength: 21
  minLength: 4
  example: '1000.0400'
  description: Valor relacionado ao objeto.
  currency:
  type: string
  pattern: '^[A-Z]{3}$'
  maxLength: 3
  description: 'Moeda referente ao valor monetário, seguindo o modelo ISO-4217.'
  example: BRL
  AccountOverdraftLimitsDataOverdraftContractedLimit:
  type: object
  description: Valor do limite contratado do cheque especial.
  required:
  - amount
  - currency
  properties:
  amount:
  type: string
  format: double
  pattern: '^\d{1,15}\.\d{2,4}$'
  maxLength: 20
  minLength: 4
  example: '1000.0400'
  description: Valor relacionado ao objeto.
  currency:
  type: string
  pattern: '^[A-Z]{3}$'
  maxLength: 3
  description: 'Moeda referente ao valor monetário, seguindo o modelo ISO-4217.'
  example: BRL
  AccountOverdraftLimitsDataOverdraftUsedLimit:
  type: object
  description: Valor utilizado total do limite do cheque especial e o adiantamento a depositante.
  required:
  - amount
  - currency
  properties:
  amount:
  type: string
  format: double
  pattern: '^\d{1,15}\.\d{2,4}$'
  maxLength: 20
  minLength: 4
  example: '1000.0400'
  description: Valor relacionado ao objeto.
  currency:
  type: string
  pattern: '^[A-Z]{3}$'
  maxLength: 3
  description: 'Moeda referente ao valor monetário, seguindo o modelo ISO-4217.'
  example: BRL
  AccountOverdraftLimitsDataUnarrangedOverdraftAmount:
  type: object
  description: Valor de operação contratada em caráter emergencial para cobertura de saldo devedor em conta de depósitos à vista e de excesso sobre o limite pactuado de cheque especial.
  required:
  - amount
  - currency
  properties:
  amount:
  type: string
  format: double
  pattern: '^\d{1,15}\.\d{2,4}$'
  maxLength: 20
  minLength: 4
  example: '1000.0400'
  description: Valor relacionado ao objeto.
  currency:
  type: string
  pattern: '^[A-Z]{3}$'
  maxLength: 3
  description: 'Moeda referente ao valor monetário, seguindo o modelo ISO-4217.'
  example: BRL
  AccountBalancesDataBlockedAmount:
  type: object
  description: 'Saldo bloqueado, não disponível para utilização imediata, por motivo de bloqueio apresentado para o cliente nos canais eletrônicos. Expresso em valor monetário com no mínimo 2 casas e no máximo 4 casas decimais.'
  required:
  - amount
  - currency
  properties:
  amount:
  type: string
  format: double
  pattern: '^\d{1,15}\.\d{2,4}$'
  maxLength: 20
  minLength: 4
  example: '1000.0400'
  description: Valor relacionado ao objeto.
  currency:
  type: string
  pattern: '^[A-Z]{3}$'
  maxLength: 3
  description: 'Moeda referente ao valor monetário, seguindo o modelo ISO-4217.'
  example: BRL
  AccountBalancesDataAvailableAmount:
  type: object
  description: 'Saldo disponível para utilização imediata. No caso de conta de depósito a vista, sem considerar cheque especial e investimentos atrelados a conta. Expresso em valor monetário com no mínimo 2 casas e no máximo 4 casas decimais.'
  required:
  - amount
  - currency
  properties:
  amount:
  type: string
  format: double
  pattern: '^-?\d{1,15}\.\d{2,4}$'
  maxLength: 21
  minLength: 4
  example: '1000.0400'
  description: Valor relacionado ao objeto.
  currency:
  type: string
  pattern: '^[A-Z]{3}$'
  maxLength: 3
  description: 'Moeda referente ao valor monetário, seguindo o modelo ISO-4217.'
  example: BRL
  AccountBalancesData:
  type: object
  description: |
  Conjunto de informações das Contas de: depósito à vista, poupança e de pagamento pré-paga
  required:
  - availableAmount
  - blockedAmount
  - automaticallyInvestedAmount
  - updateDateTime
  properties:
  availableAmount:
  $ref: '#/components/schemas/AccountBalancesDataAvailableAmount'
  blockedAmount:
  $ref: '#/components/schemas/AccountBalancesDataBlockedAmount'
  automaticallyInvestedAmount:
  $ref: '#/components/schemas/AccountBalancesDataAutomaticallyInvestedAmount'
  updateDateTime:
  type: string
  format: date-time
  maxLength: 20
  pattern: '^(\d{4})-(1[0-2]|0?[1-9])-(3[01]|[12][0-9]|0?[1-9])T(?:[01]\d|2[0123]):(?:[012345]\d):(?:[012345]\d)Z$'
  example: '2021-05-21T08:30:00Z'
  description: |
  Data e hora da última atualização do saldo. É esperado que a instituição informe a última vez que capturou o saldo para compartilhamento no Open Finance. Dessa forma, é possível que:
  - Caso a instituição capture dados de forma síncrona essa informação seja de poucos momentos;
  - Caso a instituição capture dados de forma assíncrona essa informação seja de horas ou dias no passado;
  - Quando não existente uma data vinculada especificamente ao bloco, se assume a data e hora de atualização do cadastro como um todo.

          De toda forma, é preciso continuar respeitando o prazo máximo de tempestividade da API de Contas.
  AccountData:
  type: object
  required:
  - brandName
  - companyCnpj
  - type
  - compeCode
  - number
  - checkDigit
  - accountId
  properties:
  brandName:
  type: string
  description: 'Nome da Marca reportada pelo participante no Open Finance. Recomenda-se utilizar, sempre que possível, o mesmo nome de marca atribuído no campo do diretório Customer Friendly Server Name (Authorisation Server).'
  maxLength: 80
  pattern: '[\w\W\s]*'
  example: Organização A
  companyCnpj:
  type: string
  maxLength: 14
  pattern: '^\d{14}$'
  description: 'Número completo do CNPJ da instituição responsável pelo Cadastro - o CNPJ corresponde ao número de inscrição no Cadastro de Pessoa Jurídica. Deve-se ter apenas os números do CNPJ, sem máscara'
  example: '21128159000166'
  type:
  $ref: '#/components/schemas/EnumAccountType'
  compeCode:
  type: string
  description: 'Código identificador atribuído pelo Banco Central do Brasil às instituições participantes do STR (Sistema de Transferência de reservas).O Compe (Sistema de Compensação de Cheques e Outros Papéis) é um sistema que identifica e processa as compensações bancárias. Ele é representado por um código de três dígitos que serve como identificador de bancos, sendo assim, cada instituição bancária possui um número exclusivo'
  pattern: '^\d{3}$'
  maxLength: 3
  example: '001'
  branchCode:
  type: string
  description: |
  Código da Agência detentora da conta. (Agência é a dependência destinada ao atendimento aos clientes, ao público em geral e aos associados de cooperativas de crédito, no exercício de atividades da instituição, não podendo ser móvel ou transitória)

          [Restrição] Obrigatoriamente deve ser preenchido quando o campo "type" for diferente de CONTA_PAGAMENTO_PRE_PAGA.
        pattern: '^\d{4}$'
        maxLength: 4
        example: '6272'
      number:
        type: string
        description: Número da conta
        pattern: '^\d{8,20}$'
        maxLength: 20
        example: '94088392'
      checkDigit:
        type: string
        description: Dígito da conta
        pattern: '[\w\W\s]*'
        maxLength: 1
        example: '4'
      accountId:
        type: string
        description: 'Identifica de forma única  a conta do cliente, mantendo as regras de imutabilidade dentro da instituição transmissora.'
        pattern: '^[a-zA-Z0-9][a-zA-Z0-9-]{0,99}$'
        maxLength: 100
        minLength: 1
        example: '92792126019929279212650822221989319252576'
  AccountIdentificationData:
  type: object
  description: |
  Conjunto dos atributos que caracterizam as Contas de: depósito à vista, poupança e de pagamento pré-paga
  required:
  - compeCode
  - number
  - checkDigit
  - type
  - subtype
  - currency
  properties:
  compeCode:
  type: string
  maxLength: 3
  pattern: '^\d{3}$'
  description: 'Código identificador atribuído pelo Banco Central do Brasil às instituições participantes do STR (Sistema de Transferência de reservas). O número-código substituiu o antigo código COMPE. Todos os participantes do STR, exceto as Infraestruturas do Mercado Financeiro (IMF) e a Secretaria do Tesouro Nacional, possuem um número-código independentemente de participarem da Centralizadora da Compensação de Cheques (Compe).'
  example: '001'
  branchCode:
  type: string
  maxLength: 4
  pattern: '^\d{4}$'
  description: |
  Código da Agência detentora da conta. (Agência é a dependência destinada ao atendimento aos clientes, ao público em geral e aos associados de cooperativas de crédito, no exercício de atividades da instituição, não podendo ser móvel ou transitória)

          [Restrição] Obrigatoriamente deve ser preenchido quando o campo "type" for diferente de conta pré-paga.
        example: '6272'
      number:
        type: string
        maxLength: 20
        pattern: '^\d{8,20}$'
        description: |
          Número da conta
        example: '24550245'
      checkDigit:
        type: string
        maxLength: 1
        pattern: '[\w\W\s]*'
        description: |
          Dígito da conta
        example: '4'
      type:
        $ref: '#/components/schemas/EnumAccountType'
      subtype:
        $ref: '#/components/schemas/EnumAccountSubType'
      currency:
        type: string
        pattern: '^(\w{3}){1}$'
        maxLength: 3
        description: |
          Moeda referente ao valor da transação, segundo modelo ISO-4217. p.ex. 'BRL' 
          Todos os saldos informados estão representados com a moeda vigente do Brasil
        example: BRL
  AccountOverdraftLimitsData:
  type: object
  description: |
  Conjunto de informações da Conta de: depósito à vista
  properties:
  overdraftContractedLimit:
  $ref: '#/components/schemas/AccountOverdraftLimitsDataOverdraftContractedLimit'
  overdraftUsedLimit:
  $ref: '#/components/schemas/AccountOverdraftLimitsDataOverdraftUsedLimit'
  unarrangedOverdraftAmount:
  $ref: '#/components/schemas/AccountOverdraftLimitsDataUnarrangedOverdraftAmount'
  AccountTransactionsData:
  type: object
  required:
  - transactionId
  - completedAuthorisedPaymentType
  - creditDebitType
  - transactionName
  - type
  - transactionAmount
  - transactionDateTime
  properties:
  transactionId:
  type: string
  description: |
  Código ou identificador único prestado pela instituição que mantém a conta para representar a transação individual.
  O ideal é que o `transactionId` seja imutável.
  No entanto, o `transactionId` deve obedecer, no mínimo, as regras de imutabilidade propostas conforme tabela “Data de imutabilidade por tipo de transação” presente nas orientações desta API.
  maxLength: 100
  minLength: 1
  pattern: '^[a-zA-Z0-9][a-zA-Z0-9-]{0,99}$'
  example: TXpRMU9UQTROMWhZV2xSU1FUazJSMDl
  completedAuthorisedPaymentType:
  $ref: '#/components/schemas/EnumCompletedAuthorisedPaymentIndicator'
  creditDebitType:
  $ref: '#/components/schemas/EnumCreditDebitIndicator'
  transactionName:
  type: string
  maxLength: 200
  pattern: '[\w\W\s]*'
  description: |
  Literal usada na instituição financeira para identificar a transação.
  A informação apresentada precisa ser a mesma utilizada nos canais digitais da instituição (assim como o histórico de transações apresentado na tela do aplicativo ou do navegador).
  Caso a instituição possua mais de um canal digital, a informação compartilhada deve ser a do canal que apresenta a descrição mais completa possível da transação.
  Em casos onde a descrição da transação é apresentada com múltiplas linhas, todas as linhas devem ser enviadas (concatenadas) neste atributo, não sendo obrigatória a concatenação das informações já enviadas em outros atributos (ex: valor, data) do mesmo endpoint.
  Adicionalmente, o Banco Central pode determinar o formato de compartilhamento a ser adotado por uma instituição participante específica.
  example: Transferencia Enviada Lima Santos
  type:
  $ref: '#/components/schemas/EnumTransactionTypes'
  transactionAmount:
  $ref: '#/components/schemas/AccountTransactionsDataAmount'
  transactionDateTime:
  type: string
  maxLength: 24
  pattern: '(^(\d{4})-(1[0-2]|0?[1-9])-(3[01]|[12][0-9]|0?[1-9])T(?:[01]\d|2[0123]):(?:[012345]\d):(?:[012345]\d)\.(?:[0-9]){3}Z$)'
  description: |
  Data e hora original da transação.
  example: '2016-01-29T12:29:03.374Z'
  partieCnpjCpf:
  type: string
  maxLength: 14
  pattern: '^\d{11}$|^\d{14}$'
  description: |
  Identificação da pessoa envolvida na transação: pagador ou recebedor (Preencher com o CPF ou CNPJ, sem formatação). Com a IN BCB nº 371, a partir de 02/05/23, o envio das informações de identificação de contraparte tornou-se obrigatória para transações de pagamento. Para maiores detalhes, favor consultar a página `Orientações - Contas`.

          [Restrição] Quando o "type“ for preenchido com valor FOLHA_PAGAMENTO e a transmissora for a responsável pelo pagamento de salário (banco-folha), o partieCnpjCpf informado deve ser do empregador relacionado.
        example: '43908445778'
      partiePersonType:
        $ref: '#/components/schemas/EnumPartiePersonType'
      partieCompeCode:
        type: string
        maxLength: 3
        pattern: '^\d{3}$'
        description: 'Código identificador atribuído pelo Banco Central do Brasil às instituições participantes do STR (Sistema de Transferência de reservas) referente à pessoa envolvida na transação. O número-código substituiu o antigo código COMPE. Todos os participantes do STR, exceto as Infraestruturas do Mercado Financeiro (IMF) e a Secretaria do Tesouro Nacional, possuem um número-código independentemente de participarem da Centralizadora da Compensação de Cheques (Compe).'
        example: '001'
      partieBranchCode:
        type: string
        maxLength: 4
        pattern: '^\d{4}$'
        description: 'Código da Agência detentora da conta da pessoa envolvida na transação. (Agência é a dependência destinada ao atendimento aos clientes, ao público em geral e aos associados de cooperativas de crédito, no exercício de atividades da instituição, não podendo ser móvel ou transitória)'
        example: '6272'
      partieNumber:
        type: string
        maxLength: 20
        pattern: '^\d{8,20}$'
        description: Número da conta da pessoa envolvida na transação
        example: '67890854360'
      partieCheckDigit:
        type: string
        maxLength: 1
        pattern: '[\w\W\s]*'
        description: Dígito da conta da pessoa envolvida na transação
        example: '4'
  AccountTransactionsDataAmount:
  type: object
  description: Valor da transação. Expresso em valor monetário com no mínimo 2 casas e no máximo 4 casas decimais.
  required:
  - amount
  - currency
  properties:
  amount:
  type: string
  format: double
  pattern: '^\d{1,15}\.\d{2,4}$'
  maxLength: 20
  minLength: 4
  example: '1000.0400'
  description: Valor relacionado ao objeto.
  currency:
  type: string
  pattern: '^[A-Z]{3}$'
  maxLength: 3
  description: 'Moeda referente ao valor monetário, seguindo o modelo ISO-4217.'
  example: BRL
  EnumAccountSubType:
  type: string
  enum:
  - INDIVIDUAL
  - CONJUNTA_SIMPLES
  - CONJUNTA_SOLIDARIA
  description: |
  Subtipo de conta (vide Enum):
  Conta individual - possui um único titular
  Conta conjunta simples - onde as movimentações financeiras só podem serem realizadas mediante autorização de TODOS os correntistas da conta.
  Conta conjunta solidária - é a modalidade cujos titulares podem realizar movimentações de forma isolada, isto é, sem que seja necessária a autorização dos demais titulares
  example: INDIVIDUAL
  EnumAccountType:
  type: string
  enum:
  - CONTA_DEPOSITO_A_VISTA
  - CONTA_POUPANCA
  - CONTA_PAGAMENTO_PRE_PAGA
  description: |
  Tipos de contas. Modalidades tradicionais previstas pela Resolução 4.753, não contemplando contas vinculadas, conta de domiciliados no exterior, contas em moedas estrangeiras e conta correspondente moeda eletrônica. Vide Enum
  Conta de depósito à vista ou Conta corrente - é o tipo mais comum. Nela, o dinheiro fica à sua disposição para ser sacado a qualquer momento. Essa conta não gera rendimentos para o depositante
  Conta poupança - foi criada para estimular as pessoas a pouparem. O dinheiro que ficar na conta por trinta dias passa a gerar rendimentos, com isenção de imposto de renda para quem declara. Ou seja, o dinheiro “cresce” (rende) enquanto ficar guardado na conta. Cada depósito terá rendimentos de mês em mês, sempre no dia do mês em que o dinheiro tiver sido depositado
  Conta de pagamento pré-paga: segundo CIRCULAR Nº 3.680, BCB de  2013, é a 'destinada à execução de transações de pagamento em moeda eletrônica realizadas com base em fundos denominados em reais previamente aportados'
  example: CONTA_DEPOSITO_A_VISTA
  EnumCompletedAuthorisedPaymentIndicator:
  type: string
  description: |
  Indicador da transação:
  - Transação efetivada: a transação atinge esse status quando o `transactionId` torna-se imutável;
  - Lançamento futuro: a transação será efetivada em momento futuro, ou seja, o `transactionId` pode mudar;
  - Transação processando: a transação está em processamento, ou seja, o `transactionId` pode mudar.
  enum:
  - TRANSACAO_EFETIVADA
  - LANCAMENTO_FUTURO
  - TRANSACAO_PROCESSANDO
  example: TRANSACAO_EFETIVADA
  EnumCreditDebitIndicator:
  type: string
  description: |
  Indicador do tipo de lançamento:
  Débito (no extrato) Em um extrato bancário, os débitos, marcados com a letra “D” ao lado do valor registrado, informam as saídas de dinheiro na conta-corrente.
  Crédito (no extrato) Em um extrato bancário, os créditos, marcados com a letra “C” ao lado do valor registrado, informam as entradas de dinheiro na conta-corrente.
  enum:
  - CREDITO
  - DEBITO
  example: DEBITO
  EnumPartiePersonType:
  type: string
  enum:
  - PESSOA_NATURAL
  - PESSOA_JURIDICA
  example: PESSOA_NATURAL
  description: |
  Identificação do Tipo de Pessoa da pessoa envolvida na transação.   
  Pessoa Natural - Informar CPF no campo “partieCnpjCpf”.   
  Pessoa Jurídica - Informar CNPJ no campo “partieCnpjCpf”.
  EnumTransactionTypes:
  type: string
  description: |
  O campo deve classificar a transação em um dos tipos descritos.
  O transmissor deve classificar as transações disponíveis associando-a a um dos itens do Enum listado neste campo.
  A opção OUTROS só deve ser utilizada para os casos em que de fato a transação compartilhada não possa ser classificada como um dos itens deste Enum.
  Por exemplo no caso de recebimento de pensão alimentícia.
  enum:
  - TED
  - DOC
  - PIX
  - TRANSFERENCIA_MESMA_INSTITUICAO
  - BOLETO
  - CONVENIO_ARRECADACAO
  - PACOTE_TARIFA_SERVICOS
  - TARIFA_SERVICOS_AVULSOS
  - FOLHA_PAGAMENTO
  - DEPOSITO
  - SAQUE
  - CARTAO
  - ENCARGOS_JUROS_CHEQUE_ESPECIAL
  - RENDIMENTO_APLIC_FINANCEIRA
  - PORTABILIDADE_SALARIO
  - RESGATE_APLIC_FINANCEIRA
  - OPERACAO_CREDITO
  - OUTROS
  example: PIX
  Links:
  type: object
  description: Referências para outros recusos da API requisitada.
  required:
  - self
  properties:
  self:
  type: string
  format: url
  maxLength: 2000
  description: URI completo que gerou a resposta atual.
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  first:
  type: string
  format: url
  maxLength: 2000
  description: URI da primeira página que originou essa lista de resultados. Restrição - Obrigatório quando não for a primeira página da resposta
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  prev:
  type: string
  format: url
  maxLength: 2000
  description: "URI da página anterior dessa lista de resultados. Restrição - \tObrigatório quando não for a primeira página da resposta"
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  next:
  type: string
  format: url
  maxLength: 2000
  description: URI da próxima página dessa lista de resultados. Restrição - Obrigatório quando não for a última página da resposta
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  last:
  type: string
  format: url
  maxLength: 2000
  description: URI da última página dessa lista de resultados. Restrição - Obrigatório quando não for a última página da resposta
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  LinksAccountId:
  type: object
  description: Referências para outros recusos da API requisitada.
  required:
  - self
  properties:
  self:
  type: string
  format: url
  maxLength: 2000
  description: URI completo que gerou a resposta atual.
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  first:
  type: string
  format: url
  maxLength: 2000
  description: URI da primeira página que originou essa lista de resultados. Restrição - Obrigatório quando não for a primeira página da resposta
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  prev:
  type: string
  format: url
  maxLength: 2000
  description: "URI da página anterior dessa lista de resultados. Restrição - \tObrigatório quando não for a primeira página da resposta"
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  next:
  type: string
  format: url
  maxLength: 2000
  description: URI da próxima página dessa lista de resultados. Restrição - Obrigatório quando não for a última página da resposta
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  last:
  type: string
  format: url
  maxLength: 2000
  description: URI da última página dessa lista de resultados. Restrição - Obrigatório quando não for a última página da resposta
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  TransactionsLinks:
  type: object
  description: Referências para outros recusos da API requisitada.
  required:
  - self
  properties:
  self:
  type: string
  format: url
  maxLength: 2000
  description: URI completo que gerou a resposta atual.
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  first:
  type: string
  format: url
  maxLength: 2000
  description: URI da primeira página que originou essa lista de resultados. Restrição - Obrigatório quando não for a primeira página da resposta
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  prev:
  type: string
  format: url
  maxLength: 2000
  description: "URI da página anterior dessa lista de resultados. Restrição - \tObrigatório quando não for a primeira página da resposta"
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  next:
  type: string
  format: url
  maxLength: 2000
  description: URI da próxima página dessa lista de resultados. Restrição - Obrigatório quando não for a última página da resposta
  example: 'https://api.banco.com.br/open-banking/api/v2/resource'
  Meta:
  type: object
  description: Meta informações referente à API requisitada.
  required:
  - totalRecords
  - totalPages
  - requestDateTime
  properties:
  totalRecords:
  type: integer
  format: int32
  description: Número total de registros no resultado
  example: 1
  totalPages:
  type: integer
  format: int32
  description: Número total de páginas no resultado
  example: 1
  requestDateTime:
  description: 'Data e hora da consulta, conforme especificação RFC-3339, formato UTC.'
  type: string
  maxLength: 20
  format: date-time
  example: '2021-05-21T08:30:00Z'
  MetaOnlyRequestDateTime:
  type: object
  description: Meta informações referente à API requisitada.
  required:
  - requestDateTime
  properties:
  requestDateTime:
  description: 'Data e hora da consulta, conforme especificação RFC-3339, formato UTC.'
  type: string
  maxLength: 20
  format: date-time
  example: '2021-05-21T08:30:00Z'
  ResponseAccountList:
  type: object
  required:
  - data
  - links
  - meta
  properties:
  data:
  type: array
  description: 'Lista de contas depósito à vista, poupança e pagamento pré-pagas mantidas pelo cliente na instituição transmissora e para as quais ele tenha fornecido consentimento'
  minItems: 0
  items:
  $ref: '#/components/schemas/AccountData'
  links:
  $ref: '#/components/schemas/Links'
  meta:
  $ref: '#/components/schemas/Meta'
  ResponseAccountBalances:
  type: object
  required:
  - data
  - links
  - meta
  properties:
  data:
  $ref: '#/components/schemas/AccountBalancesData'
  links:
  $ref: '#/components/schemas/Links'
  meta:
  $ref: '#/components/schemas/Meta'
  ResponseAccountIdentification:
  type: object
  required:
  - data
  - links
  - meta
  properties:
  data:
  $ref: '#/components/schemas/AccountIdentificationData'
  links:
  $ref: '#/components/schemas/LinksAccountId'
  meta:
  $ref: '#/components/schemas/Meta'
  ResponseAccountOverdraftLimits:
  type: object
  required:
  - data
  - links
  - meta
  properties:
  data:
  $ref: '#/components/schemas/AccountOverdraftLimitsData'
  links:
  $ref: '#/components/schemas/Links'
  meta:
  $ref: '#/components/schemas/Meta'
  ResponseAccountTransactions:
  type: object
  required:
  - data
  - links
  - meta
  properties:
  data:
  type: array
  description: |
  Lista dos lançamentos referentes às transações realizadas e de lançamentos futuros para as contas de: depósito à vista, poupança e de pagamento pré-paga
  minItems: 0
  items:
  $ref: '#/components/schemas/AccountTransactionsData'
  links:
  $ref: '#/components/schemas/TransactionsLinks'
  meta:
  $ref: '#/components/schemas/MetaOnlyRequestDateTime'
  ResponseErrorMetaSingle:
  type: object
  required:
  - errors
  properties:
  errors:
  type: array
  minItems: 1
  maxItems: 13
  items:
  type: object
  required:
  - code
  - title
  - detail
  properties:
  code:
  description: Código de erro específico do endpoint
  type: string
  pattern: '[\w\W\s]*'
  maxLength: 255
  title:
  description: Título legível por humanos deste erro específico
  type: string
  pattern: '[\w\W\s]*'
  maxLength: 255
  detail:
  description: Descrição legível por humanos deste erro específico
  type: string
  pattern: '[\w\W\s]*'
  maxLength: 2048
  meta:
  $ref: '#/components/schemas/MetaOnlyRequestDateTime'
  ResponseError:
  type: object
  required:
  - errors
  properties:
  errors:
  type: array
  minItems: 1
  maxItems: 13
  items:
  type: object
  required:
  - code
  - title
  - detail
  properties:
  code:
  description: Código de erro específico do endpoint
  type: string
  pattern: '[\w\W\s]*'
  maxLength: 255
  title:
  description: Título legível por humanos deste erro específico
  type: string
  pattern: '[\w\W\s]*'
  maxLength: 255
  detail:
  description: Descrição legível por humanos deste erro específico
  type: string
  pattern: '[\w\W\s]*'
  maxLength: 2048
  meta:
  $ref: '#/components/schemas/Meta'
  XFapiInteractionId:
  type: string
  format: uuid
  maxLength: 36
  pattern: '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$'
  example: d78fc4e5-37ca-4da3-adf2-9b082bf92280
  description: 'Um UUID [RFC4122](https://tools.ietf.org/html/rfc4122) usado como um ID de correlação entre request e response. Campo de geração e envio obrigatório pela receptora (client) e o seu valor deve ser “espelhado” pela transmissora (server) no cabeçalho de resposta. Caso não seja recebido ou se for recebido um valor inválido, a transmissora deve gerar um x-fapi-interaction-id e retorná-lo na resposta com o HTTP Status Code 400. A receptora deve acatar o valor recebido da transmissora.'
  parameters:
  accountId:
  name: accountId
  in: path
  description: 'Identificador da conta de depósito à vista, de poupança ou de pagamento pré-paga.'
  required: true
  schema:
  type: string
  pattern: '^[a-zA-Z0-9][a-zA-Z0-9\-]{0,99}$'
  maxLength: 100
  accountType:
  name: accountType
  description: 'Tipos de contas. Modalidades tradicionais previstas pela Resolução 4.753, não contemplando contas vinculadas, conta de domiciliados no exterior, contas em moedas estrangeiras e conta correspondente moeda eletrônica. Vide Enum.'
  required: false
  in: query
  schema:
  $ref: '#/components/schemas/EnumAccountType'
  Authorization:
  name: Authorization
  in: header
  description: Cabeçalho HTTP padrão. Permite que as credenciais sejam fornecidas dependendo do tipo de recurso solicitado
  required: true
  schema:
  type: string
  pattern: '[\w\W\s]*'
  maxLength: 2048
  creditDebitIndicator:
  name: creditDebitIndicator
  description: Indicador do tipo de lançamento
  required: false
  in: query
  schema:
  $ref: '#/components/schemas/EnumCreditDebitIndicator'
  fromBookingDate:
  name: fromBookingDate
  description: 'Data inicial de filtragem. [Restrição] Deve obrigatoriamente ser enviado caso o campo toBookingDate seja informado. Caso não seja informado, deve ser assumido o dia atual.'
  required: false
  in: query
  schema:
  type: string
  maxLength: 10
  format: date
  example: '2021-05-21'
  fromBookingDateMaxLimited:
  in: query
  name: fromBookingDate
  description: |
  Data inicial de filtragem. O período máximo utilizado no filtro é de 7 dias inclusive (D-6).   
  [Restrição] Deve obrigatoriamente ser enviado caso o campo toBookingDate seja informado.
  Caso não seja informado, deve ser assumido o dia atual.
  required: false
  schema:
  type: string
  maxLength: 10
  format: date
  example: '2021-05-21'
  toBookingDateMaxLimited:
  in: query
  name: toBookingDate
  description: |
  Data final de filtragem. O período máximo utilizado no filtro é de 7 dias inclusive (D-6).   
  [Restrição] Deve obrigatoriamente ser enviado caso o campo fromBookingDate seja informado.
  Caso não seja informado, deve ser assumido o dia atual.
  required: false
  schema:
  type: string
  maxLength: 10
  format: date
  example: '2021-05-21'
  pagination-key:
  name: pagination-key
  in: query
  description: 'Identificador de rechamada, utilizado para evitar a contagem de chamadas ao endpoint durante a paginação.'
  schema:
  type: string
  maxLength: 2048
  pattern: '[\w\W\s]*'
  page:
  name: page
  in: query
  description: Número da página que está sendo requisitada (o valor da primeira página é 1).
  schema:
  type: integer
  default: 1
  minimum: 1
  maximum: 2147483647
  format: int32
  pageSize:
  name: page-size
  in: query
  description: Quantidade total de registros por páginas.
  schema:
  type: integer
  default: 25
  minimum: 1
  format: int32
  maximum: 1000
  toBookingDate:
  name: toBookingDate
  description: 'Data final de filtragem. [Restrição] Deve obrigatoriamente ser enviado caso o campo fromBookingDate seja informado. Caso não seja informado, deve ser assumido o dia atual.'
  required: false
  in: query
  schema:
  type: string
  maxLength: 10
  format: date
  example: '2021-05-21'
  xCustomerUserAgent:
  name: x-customer-user-agent
  in: header
  description: Indica o user-agent que o usuário utiliza.
  required: false
  schema:
  type: string
  pattern: '[\w\W\s]*'
  minLength: 1
  maxLength: 100
  xFapiAuthDate:
  name: x-fapi-auth-date
  in: header
  description: 'Data em que o usuário logou pela última vez com o receptor. Representada de acordo com a [RFC7231](https://tools.ietf.org/html/rfc7231).Exemplo: Sun, 10 Sep 2017 19:43:31 UTC'
  required: false
  schema:
  type: string
  pattern: '^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), \d{2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \d{4} \d{2}:\d{2}:\d{2} (GMT|UTC)$'
  minLength: 29
  maxLength: 29
  xFapiCustomerIpAddress:
  name: x-fapi-customer-ip-address
  in: header
  description: O endereço IP do usuário se estiver atualmente logado com o receptor.
  required: false
  schema:
  type: string
  pattern: '[\w\W\s]*'
  minLength: 1
  maxLength: 100
  xFapiInteractionId:
  name: x-fapi-interaction-id
  in: header
  description: 'Um UUID [RFC4122](https://tools.ietf.org/html/rfc4122) usado como um ID de correlação entre request e response. Campo de geração e envio obrigatório pela receptora (client) e o seu valor deve ser “espelhado” pela transmissora (server) no cabeçalho de resposta. Caso não seja recebido ou se for recebido um valor inválido, a transmissora deve gerar um x-fapi-interaction-id e retorná-lo na resposta com o HTTP Status Code 400. A receptora deve acatar o valor recebido da transmissora.'
  required: true
  schema:
  type: string
  format: uuid
  minLength: 1
  maxLength: 36
  pattern: '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$'
  example: d78fc4e5-37ca-4da3-adf2-9b082bf92280
  securitySchemes:
  OpenId:
  type: openIdConnect
  openIdConnectUrl: 'https://auth.mockbank.poc.raidiam.io/.well-known/openid-configuration'
  OAuth2Security:
  type: oauth2
  description: Fluxo OAuth necessário para que a receptora tenha acesso aos dados na instituição transmissora. Requer o processo de redirecionamento e autenticação do usuário a que se referem os dados.
  flows:
  authorizationCode:
  authorizationUrl: 'https://authserver.example/authorization'
  tokenUrl: 'https://authserver.example/token'
  scopes:
  accounts: Escopo necessário para acesso à API Accounts. O controle dos endpoints específicos é feito via permissions.
  responses:
  OKResponseAccountList:
  description: Dados de identificação das contas obtidos com sucesso.
  headers:
  x-fapi-interaction-id:
  required: true
  description: 'Um UUID [RFC4122](https://tools.ietf.org/html/rfc4122) usado como um ID de correlação entre request e response. Campo de geração e envio obrigatório pela receptora (client) e o seu valor deve ser “espelhado” pela transmissora (server) no cabeçalho de resposta. Caso não seja recebido ou se for recebido um valor inválido, a transmissora deve gerar um x-fapi-interaction-id e retorná-lo na resposta com o HTTP Status Code 400. A receptora deve acatar o valor recebido da transmissora.'
  schema:
  $ref: '#/components/schemas/XFapiInteractionId'
  content:
  application/json:
  schema:
  $ref: '#/components/schemas/ResponseAccountList'
  OKResponseAccountIdentification:
  description: Dados de identificação da conta identificada por accountId obtidos com sucesso.
  headers:
  x-fapi-interaction-id:
  required: true
  description: 'Um UUID [RFC4122](https://tools.ietf.org/html/rfc4122) usado como um ID de correlação entre request e response. Campo de geração e envio obrigatório pela receptora (client) e o seu valor deve ser “espelhado” pela transmissora (server) no cabeçalho de resposta. Caso não seja recebido ou se for recebido um valor inválido, a transmissora deve gerar um x-fapi-interaction-id e retorná-lo na resposta com o HTTP Status Code 400. A receptora deve acatar o valor recebido da transmissora.'
  schema:
  $ref: '#/components/schemas/XFapiInteractionId'
  content:
  application/json:
  schema:
  $ref: '#/components/schemas/ResponseAccountIdentification'
  OKResponseAccountBalances:
  description: Dados relativos aos saldos da conta identificada por accountId obtidos com sucesso.
  headers:
  x-fapi-interaction-id:
  required: true
  description: 'Um UUID [RFC4122](https://tools.ietf.org/html/rfc4122) usado como um ID de correlação entre request e response. Campo de geração e envio obrigatório pela receptora (client) e o seu valor deve ser “espelhado” pela transmissora (server) no cabeçalho de resposta. Caso não seja recebido ou se for recebido um valor inválido, a transmissora deve gerar um x-fapi-interaction-id e retorná-lo na resposta com o HTTP Status Code 400. A receptora deve acatar o valor recebido da transmissora.'
  schema:
  $ref: '#/components/schemas/XFapiInteractionId'
  content:
  application/json:
  schema:
  $ref: '#/components/schemas/ResponseAccountBalances'
  OKResponseAccountTransactions:
  description: Dados da lista de transações da conta identificada por accountId obtidos com sucesso.
  headers:
  x-fapi-interaction-id:
  required: true
  description: 'Um UUID [RFC4122](https://tools.ietf.org/html/rfc4122) usado como um ID de correlação entre request e response. Campo de geração e envio obrigatório pela receptora (client) e o seu valor deve ser “espelhado” pela transmissora (server) no cabeçalho de resposta. Caso não seja recebido ou se for recebido um valor inválido, a transmissora deve gerar um x-fapi-interaction-id e retorná-lo na resposta com o HTTP Status Code 400. A receptora deve acatar o valor recebido da transmissora.'
  schema:
  $ref: '#/components/schemas/XFapiInteractionId'
  content:
  application/json:
  schema:
  $ref: '#/components/schemas/ResponseAccountTransactions'
  OKResponseAccountOverdraftLimits:
  description: Dados de limites da conta identificada por accountId obtidos com sucesso.
  headers:
  x-fapi-interaction-id:
  required: true
  description: 'Um UUID [RFC4122](https://tools.ietf.org/html/rfc4122) usado como um ID de correlação entre request e response. Campo de geração e envio obrigatório pela receptora (client) e o seu valor deve ser “espelhado” pela transmissora (server) no cabeçalho de resposta. Caso não seja recebido ou se for recebido um valor inválido, a transmissora deve gerar um x-fapi-interaction-id e retorná-lo na resposta com o HTTP Status Code 400. A receptora deve acatar o valor recebido da transmissora.'
  schema:
  $ref: '#/components/schemas/XFapiInteractionId'
  content:
  application/json:
  schema:
  $ref: '#/components/schemas/ResponseAccountOverdraftLimits'
  BadRequest:
  description: 'A requisição foi malformada, omitindo atributos obrigatórios, seja no payload ou através de atributos na URL.'
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseErrorMetaSingle'
  Forbidden:
  description: O token tem escopo incorreto ou uma política de segurança foi violada
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseErrorMetaSingle'
  GatewayTimeout:
  description: GATEWAY TIMEOUT - A requisição não foi atendida dentro do tempo limite estabelecido
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseErrorMetaSingle'
  InternalServerError:
  description: Ocorreu um erro no gateway da API ou no microsserviço
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseErrorMetaSingle'
  Locked:
  description: Locked
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseErrorMetaSingle'
  MethodNotAllowed:
  description: O consumidor tentou acessar o recurso com um método não suportado
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseErrorMetaSingle'
  NotAcceptable:
  description: A solicitação continha um cabeçalho Accept diferente dos tipos de mídia permitidos ou um conjunto de caracteres diferente de UTF-8
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseErrorMetaSingle'
  NotFound:
  description: O recurso solicitado não existe ou não foi implementado
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseErrorMetaSingle'
  TooManyRequests:
  description: 'A operação foi recusada, pois muitas solicitações foram feitas dentro de um determinado período ou o limite global de requisições concorrentes foi atingido'
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseErrorMetaSingle'
  Unauthorized:
  description: Cabeçalho de autenticação ausente/inválido ou token inválido
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseErrorMetaSingle'
  UnprocessableEntity:
  description: 'A sintaxe da requisição esta correta, mas não foi possível processar as instruções presentes.'
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseErrorMetaSingle'
  SiteIsOverloaded:
  description: 'O site está sobrecarregado e a operação foi recusada, pois foi atingido o limite máximo de TPS global, neste momento.'
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseErrorMetaSingle'
  Default:
  description: Erro inesperado.
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseErrorMetaSingle'
  BadRequestWithAdditionalProperties:
  description: 'A requisição foi malformada, omitindo atributos obrigatórios, seja no payload ou através de atributos na URL.'
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseError'
  ForbiddenWithAdditionalProperties:
  description: O token tem escopo incorreto ou uma política de segurança foi violada
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseError'
  GatewayTimeoutWithAdditionalProperties:
  description: GATEWAY TIMEOUT - A requisição não foi atendida dentro do tempo limite estabelecido
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseError'
  InternalServerErrorWithAdditionalProperties:
  description: Ocorreu um erro no gateway da API ou no microsserviço
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseError'
  LockedWithAdditionalProperties:
  description: Locked
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseError'
  MethodNotAllowedWithAdditionalProperties:
  description: O consumidor tentou acessar o recurso com um método não suportado
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseError'
  NotAcceptableWithAdditionalProperties:
  description: A solicitação continha um cabeçalho Accept diferente dos tipos de mídia permitidos ou um conjunto de caracteres diferente de UTF-8
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseError'
  NotFoundWithAdditionalProperties:
  description: O recurso solicitado não existe ou não foi implementado
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseError'
  TooManyRequestsWithAdditionalProperties:
  description: 'A operação foi recusada, pois muitas solicitações foram feitas dentro de um determinado período ou o limite global de requisições concorrentes foi atingido'
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseError'
  UnauthorizedWithAdditionalProperties:
  description: Cabeçalho de autenticação ausente/inválido ou token inválido
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseError'
  UnprocessableEntityWithAdditionalProperties:
  description: 'A sintaxe da requisição esta correta, mas não foi possível processar as instruções presentes.'
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseError'
  DefaultWithAdditionalProperties:
  description: Erro inesperado.
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseError'
  SiteIsOverloadedWithAdditionalProperties:
  description: 'O site está sobrecarregado e a operação foi recusada, pois foi atingido o limite máximo de TPS global, neste momento.'
  content:
  application/json; charset=utf-8:
  schema:
  $ref: '#/components/schemas/ResponseError'


# Estrutura de Módulos Maven - Open Finance API

## Visão Geral da Arquitetura
```
open-finance-accounts-api/
├── pom.xml (parent)
├── docker-compose.yml
├── Dockerfile
├── README.md
└── modules/
    ├── open-finance-core-domain/
    ├── open-finance-use-cases/
    ├── open-finance-adapter-api/
    ├── open-finance-adapter-persistence/
    ├── open-finance-adapter-security/
    ├── open-finance-adapter-integration/
    └── open-finance-application/
```

## Detalhamento dos Módulos

### 1. **open-finance-core-domain** (Centro da Arquitetura Hexagonal)
```
open-finance-core-domain/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── br/
                └── com/
                    └── openfinance/
                        └── domain/
                            ├── entities/
                            │   ├── Account.java
                            │   ├── AccountBalance.java
                            │   ├── AccountTransaction.java
                            │   ├── AccountOverdraftLimit.java
                            │   └── ConsentData.java
                            ├── enums/
                            │   ├── AccountType.java
                            │   ├── AccountSubType.java
                            │   ├── TransactionType.java
                            │   ├── CreditDebitIndicator.java
                            │   └── CompletedAuthorisedPaymentIndicator.java
                            ├── valueobjects/
                            │   ├── Amount.java
                            │   ├── AccountId.java
                            │   ├── TransactionId.java
                            │   ├── ConsentId.java
                            │   └── CompanyIdentification.java
                            └── exceptions/
                                ├── AccountNotFoundException.java
                                ├── InvalidConsentException.java
                                ├── UnauthorizedAccessException.java
                                └── BusinessRuleException.java
```

### 2. **open-finance-use-cases** (Casos de Uso/Regras de Negócio)
```
open-finance-use-cases/
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── br/
    │           └── com/
    │               └── openfinance/
    │                   └── usecases/
    │                       ├── ports/
    │                       │   ├── input/
    │                       │   │   ├── IGetAccountsUseCase.java
    │                       │   │   ├── IGetAccountByIdUseCase.java
    │                       │   │   ├── IGetAccountBalancesUseCase.java
    │                       │   │   ├── IGetAccountTransactionsUseCase.java
    │                       │   │   └── IGetAccountOverdraftLimitsUseCase.java
    │                       │   └── output/
    │                       │       ├── IAccountRepository.java
    │                       │       ├── IConsentValidationPort.java
    │                       │       ├── ISecurityPort.java
    │                       │       └── IExternalAccountDataPort.java
    │                       ├── services/
    │                       │   ├── GetAccountsService.java
    │                       │   ├── GetAccountByIdService.java
    │                       │   ├── GetAccountBalancesService.java
    │                       │   ├── GetAccountTransactionsService.java
    │                       │   └── GetAccountOverdraftLimitsService.java
    │                       └── validators/
    │                           ├── ConsentValidator.java
    │                           ├── PermissionValidator.java
    │                           └── DateRangeValidator.java
    └── test/
        └── java/
            └── br/
                └── com/
                    └── openfinance/
                        └── usecases/
                            └── services/
```

### 3. **open-finance-adapter-api** (Adapter REST API)
```
open-finance-adapter-api/
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── br/
    │           └── com/
    │               └── openfinance/
    │                   └── adapter/
    │                       └── api/
    │                           ├── controllers/
    │                           │   └── AccountsController.java
    │                           ├── dtos/
    │                           │   ├── request/
    │                           │   │   ├── AccountListRequestDto.java
    │                           │   │   └── TransactionListRequestDto.java
    │                           │   └── response/
    │                           │       ├── AccountListResponseDto.java
    │                           │       ├── AccountIdentificationResponseDto.java
    │                           │       ├── AccountBalancesResponseDto.java
    │                           │       ├── AccountTransactionsResponseDto.java
    │                           │       ├── AccountOverdraftLimitsResponseDto.java
    │                           │       ├── LinksDto.java
    │                           │       ├── MetaDto.java
    │                           │       └── ErrorResponseDto.java
    │                           ├── mappers/
    │                           │   ├── IAccountApiMapper.java
    │                           │   ├── IAccountBalanceApiMapper.java
    │                           │   ├── IAccountTransactionApiMapper.java
    │                           │   └── IAccountOverdraftLimitApiMapper.java
    │                           ├── handlers/
    │                           │   └── GlobalExceptionHandler.java
    │                           └── config/
    │                               ├── SwaggerConfig.java
    │                               └── WebFluxConfig.java
    └── test/
        └── java/
            └── br/
                └── com/
                    └── openfinance/
                        └── adapter/
                            └── api/
                                └── controllers/
```

### 4. **open-finance-adapter-persistence** (Adapter MongoDB)
```
open-finance-adapter-persistence/
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── br/
    │           └── com/
    │               └── openfinance/
    │                   └── adapter/
    │                       └── persistence/
    │                           ├── entities/
    │                           │   ├── AccountEntity.java
    │                           │   ├── AccountBalanceEntity.java
    │                           │   ├── AccountTransactionEntity.java
    │                           │   └── AccountOverdraftLimitEntity.java
    │                           ├── repositories/
    │                           │   ├── IAccountEntityRepository.java
    │                           │   ├── IAccountBalanceEntityRepository.java
    │                           │   ├── IAccountTransactionEntityRepository.java
    │                           │   └── IAccountOverdraftLimitEntityRepository.java
    │                           ├── adapters/
    │                           │   └── AccountRepositoryAdapter.java
    │                           ├── mappers/
    │                           │   ├── IAccountPersistenceMapper.java
    │                           │   ├── IAccountBalancePersistenceMapper.java
    │                           │   ├── IAccountTransactionPersistenceMapper.java
    │                           │   └── IAccountOverdraftLimitPersistenceMapper.java
    │                           └── config/
    │                               └── MongoConfig.java
    └── test/
        └── java/
            └── br/
                └── com/
                    └── openfinance/
                        └── adapter/
                            └── persistence/
```

### 5. **open-finance-adapter-security** (Adapter Segurança/OAuth2)
```
open-finance-adapter-security/
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── br/
    │           └── com/
    │               └── openfinance/
    │                   └── adapter/
    │                       └── security/
    │                           ├── adapters/
    │                           │   ├── SecurityAdapter.java
    │                           │   └── ConsentValidationAdapter.java
    │                           ├── config/
    │                           │   ├── SecurityConfig.java
    │                           │   ├── OAuth2Config.java
    │                           │   └── JwtConfig.java
    │                           ├── filters/
    │                           │   ├── JwtAuthenticationFilter.java
    │                           │   └── ConsentValidationFilter.java
    │                           ├── services/
    │                           │   ├── JwtService.java
    │                           │   ├── ConsentService.java
    │                           │   └── PermissionService.java
    │                           └── dtos/
    │                               ├── JwtTokenDto.java
    │                               ├── ConsentDto.java
    │                               └── PermissionDto.java
    └── test/
        └── java/
            └── br/
                └── com/
                    └── openfinance/
                        └── adapter/
                            └── security/
```

### 6. **open-finance-adapter-integration** (Adapter para Serviços Externos)
```
open-finance-adapter-integration/
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── br/
    │           └── com/
    │               └── openfinance/
    │                   └── adapter/
    │                       └── integration/
    │                           ├── adapters/
    │                           │   ├── ExternalAccountDataAdapter.java
    │                           │   └── ConsentServiceAdapter.java
    │                           ├── clients/
    │                           │   ├── IAccountDataClient.java
    │                           │   └── IConsentServiceClient.java
    │                           ├── dtos/
    │                           │   ├── external/
    │                           │   │   ├── ExternalAccountDto.java
    │                           │   │   ├── ExternalBalanceDto.java
    │                           │   │   └── ExternalTransactionDto.java
    │                           │   └── integration/
    │                           │       ├── ConsentStatusDto.java
    │                           │       └── PermissionCheckDto.java
    │                           ├── mappers/
    │                           │   ├── IExternalAccountMapper.java
    │                           │   └── IConsentIntegrationMapper.java
    │                           └── config/
    │                               ├── WebClientConfig.java
    │                               └── IntegrationConfig.java
    └── test/
        └── java/
            └── br/
                └── com/
                    └── openfinance/
                        └── adapter/
                            └── integration/
```

### 7. **open-finance-application** (Módulo Principal - Orquestração)
```
open-finance-application/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── br/
    │   │       └── com/
    │   │           └── openfinance/
    │   │               └── application/
    │   │                   ├── OpenFinanceApplication.java
    │   │                   └── config/
    │   │                       ├── ApplicationConfig.java
    │   │                       ├── BeanConfiguration.java
    │   │                       └── ProfileConfig.java
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-test.yml
    │       ├── application-prod.yml
    │       └── logback-spring.xml
    └── test/
        ├── java/
        │   └── br/
        │       └── com/
        │           └── openfinance/
        │               └── application/
        │                   └── integration/
        │                       ├── AccountsControllerIntegrationTest.java
        │                       └── ApplicationIntegrationTest.java
        └── resources/
            ├── application-test.yml
            └── test-data/
```

## Arquivos Docker

### Dockerfile (na raiz)
```dockerfile
# Multi-stage build será definido aqui
# Stage 1: Build
# Stage 2: Runtime
```

### docker-compose.yml (na raiz)
```yaml
# Serviços:
# - MongoDB
# - Redis (cache)
# - Aplicação Open Finance
# - Observabilidade (opcional)
```

## Características da Estrutura

### Vantagens desta Arquitetura:
1. **Separação Clara de Responsabilidades**: Cada módulo tem uma responsabilidade específica
2. **Baixo Acoplamento**: Modules se comunicam através de interfaces bem definidas
3. **Alta Coesão**: Funcionalidades relacionadas estão agrupadas no mesmo módulo
4. **Testabilidade**: Cada módulo pode ser testado independentemente
5. **Manutenibilidade**: Mudanças em um módulo não afetam diretamente outros
6. **Escalabilidade**: Facilita a evolução para microserviços se necessário

### Fluxo de Dependências:
```
application → adapter-api → use-cases → core-domain
           → adapter-persistence → use-cases
           → adapter-security → use-cases
           → adapter-integration → use-cases
```

### Princípios Aplicados:
- **SOLID**: Cada módulo respeita responsabilidade única, aberto/fechado, etc.
- **12 Factor App**: Configuração por ambiente, logs, processos stateless
- **Hexagonal Architecture**: Core domain isolado, adapters nas bordas
- **Clean Architecture**: Dependências apontam para dentro (domain)



Convenções de Payload
Esta seção do padrão descreve as estruturas padrões de requisição e resposta para todos os endpoints das APIs, assim como as convenções de nomenclatura para os atributos.
Estrutura de requisição
Cada requisição deve ser um objeto JSON contendo um objeto data para armazenar os dados primários da requisição.
No mesmo nível do objeto data, poderá existir um objeto meta se assim for especificado pelo endpoint. O objeto meta é usado para fornecer informações adicionais ao endpoint, como parâmetros de paginação, contagens de paginação ou outros propósitos complementares ao funcionamento da API.
A definição do conteúdo para o objeto data será definida separadamente para cada endpoint.
Estrutura da requisição
```
    {
        "data": {
            "..."
        }
    }
```

### Princípios

Os seguintes princípios técnicos não exaustivos constituem a base para o desenvolvimento e implementação das APIs para o Open Finance no Brasil.

Princípio 1: Segurança
A adoção de mecanismos de segurança no design e implementação das APIs do Open Finance no Brasil deverá considerar os padrões aplicáveis a cada uma de suas fases, visando a proteção e a disponibilidade do ecossistema como um todo, considerando clientes, participantes e os dados específicos compartilhados em cada fase.

Princípio 2: RESTful APIs
A API irá aderir aos conceitos de RESTful API sempre que for possível e sensato.

Princípio 3: Padrões existentes
Os padrões existentes serão adotados sempre que sua aplicação for relevante/apropriada e desde que não violem nenhum dos demais princípios, com foco na experiência do desenvolvedor e do usuário e, ainda, prevendo a extensibilidade, resiliência e a evolução do Open Finance no Brasil.

Princípio 4: ISO 20022
Os payloads das APIs serão desenvolvidos utilizando como base os elementos e componentes de mensagem ISO 20022, que poderão ser modificados, caso necessário, para deixar o payload mais simples e/ou atender às características locais, tal como implementado em diferentes jurisdições.

Princípio 5: Extensibilidade
Os fluxos das APIs serão estendidos para atender a casos de uso mais complexos em futuros releases e, portanto, esse princípio será mantido em mente durante o design, e os procedimentos serão detalhados durante a implementação.

Princípio 6: Códigos de status
A API usará dois códigos de status que atendem a dois propósitos diferentes: (i) o HTTP status code reflete o resultado da chamada da API e (ii) um campo status em alguns resource payloads reflete o status dos resources nos casos de acesso write (p. ex. iniciação de pagamento).

Princípio 7: Identificadores únicos
Um recurso REST deverá ter um identificador exclusivo que possa ser usado para identificar o recurso alvo da API. Esse identificador será usado para criar URLs que permitam endereçar recursos específicos, obedecendo aos padrões definidos nesta documentação, no item Formação e estabilidade do ID.

Princípio 8: Categorização dos requisitos de implementação
Quando um requisito estiver sendo implementado por um transmissor e/ou um receptor, uma categorização diferente será aplicada. As funcionalidades, endpoints e campos em cada recurso serão categorizados como 'Obrigatório', 'Condicional' ou 'Opcional'.

Princípio 9: Agnósticas
As APIs serão agnósticas à implementação onde elas poderão ser consumidas independente das tecnologias adotadas no ecossistema, porém com aderência aos princípios contidos nesta documentação.

Princípio 10: Idempotência
As APIs serão definidas como idempotentes para não causar uma experiência ruim ao consumidor ou aumentar os indicadores de risco falso positivo. Trata-se de recurso necessário para garantir que não haja duplicidade em caso de perda de comunicação e não deve se limitar aos verbos HTTP, devendo ser aplicado ao design completo da API.

### Estrutura de resposta

Cada endpoint retornará um objeto JSON contendo os atributos abaixo.
Se a resposta for bem-sucedida (200 OK), o objeto JSON irá conter:
obrigatoriamente um objeto data;
obrigatoriamente um objeto links;
opcionalmente um objeto meta, se necessário pela definição do endpoint requisitado.
Se a resposta for malsucedida (não 200 OK), o objeto JSON poderá conter:
um objeto errors (conforme a definição específica do endpoint).
A definição do conteúdo para os objetos data e meta será definida separadamente para cada endpoint.
O objeto links irá conter hypermedia (referências para recursos relacionados) para outros recursos da API requisitada.
O objeto de links sempre irá conter o atributo self que irá apontar para a URI da solicitação atual.
O objeto errors será um array de zero ou mais objetos. Os atributos deste objeto serão os descritos abaixo:
obrigatoriamente o atributo code contendo um código de erro específico do endpoint;
obrigatoriamente o atributo title contendo um título legível por humanos do erro deste erro específico;
obrigatoriamente o atributo detail contendo uma descrição legível por humanos deste erro específico;
opcionalmente o objeto meta contendo dados adicionais sobre o endpoint que seja relevante para o erro.

Estrutura de resposta
```
{
    "data": {
        "..."
    },
    "links":{
        "..."
    },
    "meta": {
        "..."
        }
    }
```

Estrutura de resposta de erros
```
    {
        "errors": [
            {
                "code": "...",
                "title": "...",
                "detail": "..."
            }
        ],
        "meta":{
            "..."
        }
    }
```

Convenções de nomenclatura de atributos
Caracteres válidos em nomes de atributos
Todos os nomes de objetos e atributos definidos nos objetos JSON de requisição e resposta devem ser nomeados seguindo o padrão camelCase, tendo seu nome composto apenas por letras (a-z, A-Z) e números (0-9).
Qualquer outro caractere não deve ser usado nos nomes dos objetos e atributos, com exceção do caractere - (hífen), que poderá ser utilizado apenas conforme descrito na seção Extensibilidade.
Estilo de nomeação de atributos
Os nomes dos objetos e atributos devem ser nomes significativos e em língua inglesa. Quando houver diferença entre inglês americano e inglês britânico no termo a ser utilizado, deverá ser utilizado o termo em inglês britânico. P. ex: utilizar o termo Post Code (Reino Unido) ao invés de Zip Code (Estados Unidos).
Arrays devem ser nomeados no plural. Demais atributos deverão ser nomeados no singular.
Convenções de propriedade dos atributos
Tipos de dados dos atributos
Cada atributo deverá estar associado a um tipo de dado. A lista de tipos de dados válidos está definida na seção tipos de dados comuns. Se um tipo de dado personalizado é necessário para um atributo, o mesmo deverá ser classificado como uma string com uma descrição clara de como o valor da propriedade deve ser interpretado.

### Limites operacionais


É facultado às instituições participantes implementarem um limite de acesso mensal por endpoint e por cliente.

Em endpoints que acessem recursos ou produtos, os limites serão também considerados por recurso ou produto.

A contabilização dos acessos deve ser realizada por:

I. mês

II. endpoint

III. objeto mais granular referenciado na chamada (consentimento/recurso/produto)

IV. cliente (CPF ou CNPJ)

V. instituição consumidora da informação

Por exemplo: uma instituição receptora ‘A’ pode acessar um endpoint ‘B’, acessando o recurso ‘C’, de um cliente “D” da instituição transmissora ‘E’, no mínimo ‘N’ vezes (ou chamadas) com sucesso por mês.

Aplicabilidade dos limites operacionais: todos endpoints das APIs do tipo Dados Cadastrais e Transacionais (conforme classificação de Tipo), destes excetuam-se aqueles endpoints das APIs de Consentimento (Consents) e Recursos (Resources). As APIs de Segurança, de Dados Abertos e de Serviços (como de Iniciação de Pagamento) não podem ter restrições de limites operacionais.

A implementação dos limites operacionais é opcional pelas instituições transmissoras, mas, uma vez que sejam implementados, devem garantir o consumo mínimo. É facultada à instituição a possibilidade de ampliar esses limites, mas vedada a implementação de limites inferiores aos estabelecidos.

Os valores mínimos de limites operacionais a serem considerados, por endpoint, que devem ser iguais ou maiores que os abaixo, de acordo com a classificação de frequência de utilização:

I. 8 chamadas ao mês, para endpoint classificado como de baixa frequência

II. 30 chamadas ao mês, para endpoint classificados como de média frequência

III. 120 chamadas ao mês, para endpoint classificado como de média-alta frequência

IV. 240 chamadas ao mês, para endpoint classificado como de alta frequência

V. 420 chamadas ao mês, para os seguintes endpoints da API de Contas: ‘Saldos da conta’ e ‘Limites da conta’

Só deverão ser contabilizadas nos limites operacionais requisições respondidas com HTTP Status Code 2XX, sendo que as requisições adicionais a um endpoint para fins de paginação não devem ser contabilizadas.

As requisições que excederem os limites operacionais deverão ser respondidas com o HTTP Status Code 423.

Todas as requisições autenticadas em endpoints sujeitos ao limite operacional devem possuir o atributo x-fapi-interaction-id preenchido no seu header pela receptora, que deve ser copiado pela transmissora nos headers da resposta. Essa definição objetiva permitir um adequado rastreamento de divergências que podem ocorrer entre transmissoras e receptoras associadas ao limite operacional.

Paginação no contexto dos limites operacionais


Para a não contabilização de rechamadas, considerando que eventuais chamadas que possuam paginação devam ser interpretadas como uma única chamada, foi estabelecido a criação de um query parameter adicional para funcionar como identificador de rechamadas.

Esse novo parâmetro deve ter o nome pagination-key.

Cabe à Transmissora criar o identificador e enviá-lo via HATEOAS no retorno da chamada.

O tempo máximo de utilização do identificador pelo Receptor é de 60 minutos.

A Transmissora deve implementar validações para garantir a coerência da utilização do identificador, de acordo com as regras de limites operacionais.

Caso a Receptora utilize um pagination-key inválido ou expirado, a Transmissora deverá gerar um novo pagination-key retornando o resultado com sucesso. Esta chamada será contabilizada para os limites operacionais.

Essa implementação deve ser realizada em todos endpoints atuais com paginação.

A Transmissora poderá gerar um único ID para controle das chamadas das próximas páginas.

### Desempenho

Deverá ser medido o tempo de resposta de cada requisição, ou seja, o tempo transcorrido entre o recebimento de uma requisição que não ultrapassa os limites de tráfego e o momento em que a requisição é completamente respondida.

Adicionalmente, esta medição deverá ser feita de maneira que os tempos medidos sejam os mais próximos possíveis dos tempos de resposta experimentados por quem fez a requisição. Neste contexto, os endpoints das APIs deverão manter o percentil 95 do tempo de resposta em no máximo:

I. 1.500ms, em endpoints classificados como de alta e média-alta frequências;

II. 2.000ms, em endpoints classificados como de média frequência;

III. 4.000ms, em endpoints classificados como de baixa frequência.

Por exemplo, em um dia que um endpoint de alta frequência receba 10.000 requisições, o tempo de resposta de pelo menos 9.500 requisições deve ser inferior a 1.500ms.

### Timeout

Definição do limite do tempo de espera (timeout) do servidor (server) pelo cliente (client) em 15 segundos.

Definição do erro HTTP Status Code 504 (Gateway Timeout) como resposta do atingimento de timeout pelo servidor.

Todos os endpoints das APIs classificadas como ‘Dados Abertos’, ‘Dados Cadastrais e Transacionais’ e ‘Relatórios e métricas’, deverão satisfazer requisitos mínimos de disponibilidade abaixo. Cada um de seus endpoints deverá estar disponível:

I.95% do tempo a cada 24 horas;

II.99,5% do tempo a cada 3 meses (calculado de acordo com os dados enviados pelas instituições).

Sendo que para o cálculo da disponibilidade diária considera-se:

![img.png](img.png)


As métricas devem respeitar a individualidade do endpoint implementado pela instituição, isto é: deve-se enviar cada endpoint separadamente, conforme exemplo abaixo, no máximo nível de granularidade:

i. https://example.api/open-banking/financings/v2/contracts

ii. https://example.api/open-banking/financings/v2/contracts/contractId

iii. https://example.api/open-banking/financings/v2/contracts/contractId/warranties

iv. https://example.api/open-banking/financings/v2/contracts/contractId/scheduled-instalments

v. https://example.api/open-banking/financings/v2/contracts/contractId/payments

vi. https://example.api/open-banking/financings/v2/contracts/{contractId}

vii. https://example.api/open-banking/financings/v2/contracts/{contractId}/warranties

viii. https://example.api/open-banking/financings/v2/contracts/{contractId}/scheduled-instalments

ix. https://example.api/open-banking/financings/v2/contracts/{contractId}/payments

Observação: Endpoint com path variable, a exemplo {contractId}, podem ser enviados com ou sem chaves {}.

Todos os endpoints das APIs classificadas como ‘Serviços’ deverão possuir a mesma disponibilidade do arranjo de pagamento ou do serviço aos quais estão associadas.

A disponibilidade é checada no endpoint GET /discovery/status, conforme documentada no item API de Status.

A cada 30 segundos, a API de status é requisitada com timeout de 1s.

Será considerado uptime, se o retorno for:

OK.

Será considerado downtime, se o retorno for:

PARTIAL_FAILURE

SCHEDULED_OUTAGE

Se a requisição for realizada entre o período de 01h e 07h, o contador de SCHEDULED_OUTAGE é iniciado com 30 segundos acrescidos

Cada nova requisição vai adicionando 30 segundos ao contador de SCHEDULED_OUTAGE, até que uma requisição volte outro valor ou a requisição for feita depois das 7h

UNAVAILABLE

Se a requisição for realizada entre o período de 7h e 1h

Se serviço não responder a requisição

O contador de downtime é iniciado com 30 segundos acrescidos

Cada nova requisição adicionará 30 segundos ao contador de downtime, até que uma requisição retorne OK

O downtime deve ser calculado como o número total de segundos simultâneos por requisição da API, por período de 24 horas, começando e terminando à meia-noite, que qualquer endpoint da API não esteja disponível, dividido por 86.400 (total de segundos em 24 horas) e expresso como uma porcentagem.

A disponibilidade é calculada sendo 100% menos a quantidade em percentual da indisponibilidade.

De modo geral, consideram-se os erros HTTP Status Code 5XX como erros do servidor, e portanto, atribuíveis ao servidor das APIs

Erros baseados em HTTP Status Code 4XX são, em grande parte, atribuídos a ações ou falhas dos clientes, e desta forma, não devem ser incluídos no cálculo


### Referência de métricas

https://openfinancebrasil.atlassian.net/wiki/spaces/OF/pages/17957025/Refer+ncia


### Limites de tráfego

Limites por origem - TPM (Transações por minuto)


Limites por origem - TPM (Transações por minuto)

Existem dois tipos de origem quando analisamos o tráfego de dados, desconsiderando o tráfego interno da instituição:

IP: aplicado aos endpoints que não demandam autenticação;

Instituição ou organizationId: aplicado aos endpoints autenticados.

Os limites, quando implementados, são definidos por endpoint e por origem como segue:

Alta frequência:

De acordo com o número de consentimentos possuídos por uma determinada instituição receptora de dados com uma determinada instituição transmissora de dados:

Quando a quantidade de consentimentos ativos for ≤ 6 milhões, aplica-se a tabela:

QCA | TPM

0 < QCA ≤ 1 000 000 → 2.500

1 000 000 < QCA ≤ 2 000 000 → 5.000

2 000 000 < QCA ≤ 3 000 000 → 8.000

3 000 000 < QCA ≤ 6 000 000 → 10.000

Para QCA > 6 000 000, o TPM é calculado em faixas de 2 milhões, somando-se 2.000 ao limite da faixa anterior (ex.: 6-8 MM → 12.000 TPM)

Média-alta frequência: 2.000 TPM

Média frequência: 1.500 TPM

Baixa frequência: 1.000 TPM

Para contabilização dos limites de tráfego, tanto transmissoras como receptoras, devem considerar minuto cheio, ou seja, é preciso zerar o contador a cada minuto. Assim, tipicamente, a contabilização deve começar no início do minuto (00s000ms) e terminar no final do minuto (59s999ms). Como por exemplo:

O servidor começa a receber requisições em 10h25m55s123ms. O servidor deve contabilizar até 10h25m59s999ms;

Deve-se contabilizar a cada minuto: 10h25m00s000ms até 10h25m59s999ms, depois zerar e contar 10h26m00s000ms até 10h26m59s999ms.

Nos casos abaixo não são aplicáveis limites por origem:

APIs de Segurança:

Token: consumo OAuth 2.0 (FAPI)

Token: DCR/DCM

APIs de Consentimento (Consents)

APIs de Recursos (Resources)

Grupo de APIs de Serviços

O erro HTTP Status Code 429 (Too many requests) passa a ser utilizado de forma exclusiva para pedidos de consumo que excedam os limites de TPM.

Por fim, as requisições que ultrapassarem os limites deverão ser desprezadas no cálculo do tempo de resposta das implementações das APIs.

Limites globais - TPS (Transações por segundo)
A infraestrutura das instituições provendo APIs no Open Finance (Dados Abertos, Dados Cadastrais e Transacionais, Serviços, Relatórios e Métricas, Segurança) deve ter a capacidade de, no mínimo, atender a 300 requisições simultâneas por segundo (TPS), desconsiderando chamadas internas.

Caso o limite de 300 TPS seja atingido, no contexto do Open Finance, a instituição deve ampliar sua capacidade de infraestrutura para possibilitar um acréscimo de 150 TPS ao limite anterior. Tal aumento deve ocorrer novamente a cada vez que o limite vigente na instituição for atingido. Cada instituição deve criar monitoramento preventivo, de acordo com critérios definidos; as evidências devem estar à disposição do Banco Central do Brasil por um período de doze meses.

Fica definida a utilização do código HTTP Status Code 529 (Site is overloaded) para retorno quando atingido o limite de TPS estabelecido.

Endpoints criados dentro do conceito de extensibilidade, sejam dentro de novas APIs ou em APIs existentes, não podem ser considerados para controle do limite global de transações simultâneas.

Gatilho para ampliação do TPS

O aumento do limite deve ocorrer novamente, a cada vez que o limite vigente naquela instituição for atingido

Cada instituição precisará realizar este cálculo preventivamente

Caso a instituição tenha aumentado a sua infraestrutura para disponibilizar um TPS maior do que os 300, mas tenha elasticidade para diminuir de acordo com a demanda, poderá fazer isso até o limite mínimo de 300 TPS

Formato de cálculo para monitoramento:

Limite máximo de 10% de atingimento de 90% do TPS estabelecido por quinzena, em 3 quinzenas consecutivas

Cálculo de TPS a cada 1 segundo

Contador de segundos com TPS maior que 90% do regulamento atual (inicial 270 TPS)

Caso ao final da quinzena o contador atinja um número maior que 10%, a quinzena deve ser contabilizada

Caso a instituição contabilize 3 quinzenas consecutivas ultrapassando o limite percentual, o TPS deve ser acrescido em 150 TPS ao limite anterior

O tempo para adequação deve ser de no máximo 2 meses após a identificação do gatilho

### Paginação

Cada recurso de cada API pode possuir ou não paginação, caso a quantidade de registros retornados justifique a mesma. A paginação estará disponível e deverá funcionar independente se o recurso permite filtros por query ou POST. Isso é, filtros e paginação são aplicados de forma independente.

Parâmetros de Requisição
Quando existir paginação para o recurso deverão ser utilizados os parâmetros de query abaixo para a navegação dos resultados:

Parâmetro

Descrição

Valor Padrão

page

Número da página que está sendo requisitada (o valor da primeira página é 1).

1

page-size

Quantidade total de registros por páginas.

25

O valor padrão será assumido sempre que o parâmetro não estiver preenchido ou estiver nulo.



Exemplo de query com paginação



GET {uri}?page=1&page-size=25


Atributos de Resposta
Além dos dados requisitados, as respostas paginadas também terão em sua estrutura dois objetos adicionais que incluirão parâmetros para facilitar a navegação das páginas:


Exemplo de paginação

Apenas uma página



{
"data": {
"..."
},
"links": {
"self": "https://api.banco.com.br/open-banking/channels/v1/branches",
},
"meta": {
"totalRecords": 1,
"totalPages": 1
}
}




Links
Os links devem sempre ser validados de acordo com o pattern publicado no swagger.

Quando preenchidos devem ter a mesma estrutura (schema, host, api, versão e recurso) do que esta sendo paginado.

No objeto links, serão retornadas hypermedia (referências para os recursos relacionados) de paginação conforme parâmetros abaixo:

Parâmetro

Descrição

Restrição

first

A URI para requisitar a primeira página.

Obrigatório se a resposta não for a primeira página.

last

A URI para requisitar a última página.

Obrigatório se a resposta não for a última página e quando este campo for especificado no endpoint.

prev

A URI para requisitar a página anterior.

Obrigatório quando houver página anterior. Não deve ser enviado quando for a primeira página.

next

A URI para requisitar a próxima página.

Obrigatório quando houver página posterior. Não deve ser enviado quando for a última página.

self

A URI para requisitar a própria página

Quando chamado o método GET deverá retornar o link para o próprio recurso consultado

Quando chamado o método POST deverá retornar o link para o recurso criado

Quando chamado método PATCH deverá retornar o link para o recurso alterado



Primeira página



{
"data": {
"..."
},
"links": {
"self": "https://api.banco.com.br/open-banking/channels/v1/branches?page=1&page-size=25",
"next": "https://api.banco.com.br/open-banking/channels/v1/branches?page=2&page-size=25",
"last": "https://api.banco.com.br/open-banking/channels/v1/branches?page=10&page-size=25"
},
"meta": {
"totalRecords": 250,
"totalPages": 10
}
}


Meta
No objeto meta, serão retornadas informações sobre o total de registros disponíveis

Parâmetro

Descrição

Restrição

totalRecords

O número total de registros da requisição.

Este atributo é obrigatório.

totalPages

O número total de páginas da requisição.

Este atributo é obrigatório. Se não possuir nenhum registro o valor deve ser 0.

Para cada um desses atributos o tamanho da página especificado na requisição deverá ser utilizado para o cálculo dos valores.

Última Página



{
"data": {
"..."
},
"links": {
"self": "https://api.banco.com.br/open-banking/channels/v1/branches?page=10&page-size=25",
"first": "https://api.banco.com.br/open-banking/channels/v1/branches?page=1&page-size=25",
"prev": "https://api.banco.com.br/open-banking/channels/v1/branches?page=9&page-size=25"
},
"meta": {
"totalRecords": 250,
"totalPages": 10
}


Regras Adicionais
O tamanho máximo da página (page-size) é 1000 registros para qualquer endpoint (a menos que na API esteja especificado outros valores).

Caso a instituição transmissora/detentora defina um tamanho máximo de página (page-size) inferior, se for requisitado uma quantidade de registros maior do que seu limite operacional, e desde que o valor esteja de acordo o tamanho máximo permitido pela API, esta deverá responder entregando os dados e utilizando o page-size do seu limite operacional definido.

A instituição transmissora/detentora deve realizar os ajustes de paginação antes de efetivar a consulta:

Ex: Ao solicitar a segunda pagina 1000 registros, para uma instituição que trabalha com max size 800, a transmissora deve retornar os itens de 801 a 1600.

A instituição transmissora/detentora pode definir um tamanho máximo da página (page-size) inferior ao tamanho máximo permitido pela API, caso entenda necessário diminuir o limite para atender o SLA de resposta:

Para as APIs de Dados Cadastrais e Transacionais, o comportamento esperado da transmissora/detentora é que o page size mínimo é 25 e o máximo é 1000, exceções se aplicam a primeira página, quando for única, e a última página.

Exemplo: A receptora solicita 5 registros por página, entretanto, a transmissora possui 47 registros. Pelo comportamento descrito, a transmissora retornará 25 registros na primeira página de 22 registros na segunda.

Para reduzir a quantidade de requisições para endpoints que possuem paginação, recomenda-se que a transmissora responda com o máximo de itens possíveis na página, porém, respeitando o SLA definido para o endpoint e o tamanho máximo da página.


### Formação e Estabilidade do ID


Os IDs de recursos devem atender as seguintes regras:​
O ID de um recurso deve ser especificado no endpoint de uma API apenas para obter detalhes do recurso ou para realizar alterações no mesmo.
Se o ID for especificado nos padrões do Open Finance, então ele é obrigatório e deverá ser fornecido pela entidade implementadora da API de acordo com o padrão definido.
Se um ID for especificado, o mesmo deverá ser totalmente desconectado de significados com outras entidades. Por exemplo, um ID não deve ser uma combinação de outros campos ou uma string que possa ter conteúdo sensível que possa ser extraído.
Os IDs devem ser únicos, porém sua unicidade pode estar dentro de um contexto. Por exemplo, um ID de conta corrente deve ser único, porém apenas dentro do contexto de conta corrente.​
Nos payloads o nome de campo "id" nunca deverá ser utilizado. Cada campo ID deverá ter um nome significativo, dessa forma independentemente de onde o ID for utilizado entre múltiplos endpoints, ele sempre irá se referir ao seu objeto principal. Por exemplo, IDs para conta deverão ser representados no JSON como "accountId".
O identificador único deve se manter estável e imutável independente da data de consulta e do consentimento. Podem existir casos de exceção e estes estarão bem documentados no Swagger das APIs.
Princípios para a formação de IDs (identificadores) de recursos nas APIs
O ID DEVE ser uma string com limitação de 100 caracteres - limite que poderá ser revisitado em caso de necessidade apresentada por quaisquer dos participantes do Open Finance Brasil e - aderente aos padrões apresentados na seção 2.1 da RFC 2141;
Uma vez que será trafegado nas chamadas de interface, o ID NÃO DEVE conter dados classificados como PII - Personally identifiable information (Informação Pessoalmente Identificável) ou Informação de Identificação Pessoal;
DEVE ser garantida a unicidade do ID dentro do contexto da API assim como a estabilidade do mesmo, sendo a estabilidade condicionada à manutenção das características de identificação natural do recurso em questão (por exemplo, as informações de banco, agência e conta na API de Contas). A alteração das características de identificação natural implica na geração de um novo identificador associado ao recurso;
A utilização de meios técnicos razoáveis e disponíveis para a formação do ID é de livre implementação por parte da instituição transmissora dos dados, de forma que apenas a aderência aos princípios elencados nesta documentação é mandatória
