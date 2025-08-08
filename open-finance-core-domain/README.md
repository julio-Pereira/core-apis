# Open Finance Core Domain Module

Este módulo contém o núcleo do domínio da aplicação Open Finance, implementando os conceitos centrais de negócio de acordo com a especificação da API de Accounts do Open Finance Brasil.

## Estrutura do Módulo

### Entidades (`entity/`)

#### `Account.java`
- Entidade principal representando uma conta financeira
- Suporte para contas de depósito à vista, poupança e pré-pagas
- Implementa regras de negócio para validação de código de agência
- Utiliza Builder pattern para criação imutável

#### `AccountBalance.java`
- Representa os saldos de uma conta (disponível, bloqueado, investido automaticamente)
- Implementa cálculos de saldo efetivo e verificação de fundos
- Garante consistência de moeda entre os diferentes tipos de saldo

#### `Transaction.java`
- Representa transações financeiras com todos os atributos da API
- Implementa regras de negócio para diferentes tipos de transação
- Suporte para informações de contraparte (partie)
- Validação de estados de transação (processando, efetivada, futuro)

#### `OverdraftLimits.java`
- Representa limites de cheque especial para contas à vista
- Cálculos de limite disponível e percentual de utilização
- Suporte para descoberto não autorizado (unarranged overdraft)

### Value Objects (`valueobject/`)

#### `AccountId.java`
- Identificador único e imutável de conta
- Validação de formato conforme especificação Open Finance

#### `TransactionId.java`
- Identificador único e imutável de transação
- Implementa regras de imutabilidade baseadas no tipo de transação

#### `Amount.java`
- Representa valores monetários com moeda
- Operações aritméticas seguras (adição, subtração, multiplicação)
- Validação de consistência de moeda
- Precisão de 4 casas decimais

#### `Currency.java`
- Representa códigos de moeda seguindo ISO-4217
- Suporte nativo para BRL (Real Brasileiro)
- Validação de formato de 3 letras maiúsculas

#### `CompeCode.java`
- Código identificador de 3 dígitos do Banco Central
- Validação de formato numérico

#### `BranchCode.java`
- Código de agência bancária de 4 dígitos
- Validação de formato numérico

### Enumerações (`enums/`)

#### `AccountType.java`
- Tipos de conta: depósito à vista, poupança, pré-paga
- Métodos auxiliares para validação de regras específicas

#### `AccountSubType.java`
- Subtipos: individual, conjunta simples, conjunta solidária

#### `TransactionType.java`
- Todos os tipos de transação da especificação Open Finance
- Regras de imutabilidade por tipo (D0 vs D+1)

#### `CompletedAuthorisedPaymentIndicator.java`
- Estados de transação: efetivada, processando, lançamento futuro

#### `CreditDebitIndicator.java`
- Indicador de crédito ou débito

#### `PartiePersonType.java`
- Tipo de pessoa da contraparte: física ou jurídica

### Exceções (`exception/`)

#### `DomainException.java`
- Exceção base para todas as exceções de domínio
- Inclui código de erro estruturado

#### Exceções Específicas
- `AccountNotFoundException` - Conta não encontrada
- `TransactionNotFoundException` - Transação não encontrada
- `InvalidAccountTypeException` - Tipo de conta inválido para operação
- `InvalidTransactionStateException` - Estado de transação inválido
- `InsufficientFundsException` - Fundos insuficientes
- `CurrencyMismatchException` - Incompatibilidade de moeda
- `InvalidAmountException` - Valor inválido
- `InvalidIdentifierException` - Formato de identificador inválido
- `BusinessRuleViolationException` - Violação de regra de negócio

### Portas (`port/`)

#### Repositórios
- `IAccountRepository` - Operações de persistência de contas
- `IAccountBalanceRepository` - Operações de saldos
- `ITransactionRepository` - Operações de transações
- `IOverdraftLimitsRepository` - Operações de limites

#### Serviços Externos
- `IExternalAccountService` - Integração com sistemas externos
- `IConsentValidationService` - Validação de consentimentos
- `IPaginationService` - Geração e validação de chaves de paginação
- `IRateLimitService` - Controle de limites de tráfego

### Serviços de Domínio (`service/`)

#### `AccountValidationService.java`
- Validações específicas de conta
- Regras para suporte a overdraft e transações
- Validação de código de agência

#### `TransactionValidationService.java`
- Validações de transação
- Regras de imutabilidade de ID
- Validação de informações de contraparte

#### `BalanceCalculationService.java`
- Cálculos de saldo efetivo
- Projeções de saldo com transações pendentes
- Validação de limites de overdraft

#### `DateRangeValidationService.java`
- Validação de intervalos de data
- Regras específicas para transações (12 meses) e recentes (7 dias)

#### `PermissionValidationService.java`
- Validação de permissões Open Finance
- Constantes para todas as permissões da API

#### `AccountBusinessService.java`
- Serviço agregador com regras complexas de negócio
- Validações de consistência entre entidades

### Eventos de Domínio (`event/`)

#### `DomainEvent.java`
- Interface base para todos os eventos de domínio
- Metadados padrão: ID, timestamp, tipo, aggregate ID

#### Eventos Específicos
- `AccountAccessedEvent` - Acesso a conta
- `AccountBalanceRequestedEvent` - Solicitação de saldo
- `TransactionsRequestedEvent` - Solicitação de transações
- `OverdraftLimitsRequestedEvent` - Solicitação de limites
- `RateLimitExceededEvent` - Limite de tráfego excedido

## Princípios Aplicados

### Arquitetura Hexagonal
- Domínio isolado de dependências externas
- Portas (interfaces) definem contratos com o mundo externo
- Inversão de dependência através de interfaces

### Domain-Driven Design (DDD)
- Entidades com identidade e ciclo de vida
- Value Objects imutáveis para conceitos sem identidade
- Serviços de domínio para lógica que não pertence a entidades
- Eventos de domínio para comunicação entre bounded contexts

### SOLID Principles
- **S**ingle Responsibility: Cada classe tem uma responsabilidade específica
- **O**pen/Closed: Extensível via interfaces, fechado para modificação
- **L**iskov Substitution: Implementações podem substituir interfaces
- **I**nterface Segregation: Interfaces específicas por necessidade
- **D**ependency Inversion: Dependência de abstrações, não implementações

### Imutabilidade
- Entidades e Value Objects são imutáveis após criação
- Builder pattern para construção de objetos complexos
- Operações retornam novas instâncias quando necessário

### Validação Rica
- Validações de domínio no momento da criação
- Uso de Bean Validation (Jakarta) para validações básicas
- Validações de negócio em construtores e métodos de domínio

## Regras de Negócio Implementadas

### Contas
- Contas pré-pagas não devem ter código de agência
- Demais tipos de conta devem ter código de agência obrigatório
- Apenas contas à vista suportam limites de overdraft

### Transações
- Validação de informações de contraparte para FOLHA_PAGAMENTO
- Regras de imutabilidade baseadas no tipo de transação
- Consistência entre tipo de pessoa e CPF/CNPJ da contraparte

### Saldos
- Consistência de moeda entre diferentes tipos de saldo
- Valores bloqueados e investidos automaticamente não podem ser negativos
- Cálculos de saldo efetivo considerando overdraft

### Limites de Overdraft
- Limite usado não pode exceder limite contratado
- Todos os valores devem ter a mesma moeda
- Apenas contas à vista podem ter limites de overdraft

### Intervalos de Data
- Transações: máximo 12 meses no passado + 12 meses no futuro
- Transações recentes: máximo 7 dias no passado + 12 meses no futuro
- Data inicial não pode ser posterior à data final

## Testes

O módulo está preparado para testes unitários com:
- JUnit 5 para estrutura de testes
- Mockito para mocks de dependências
- Cobertura de todas as regras de negócio
- Testes de validação de entrada
- Testes de exceções de domínio

## Dependências

- Jakarta Validation API para validações
- JUnit 5 e Mockito para testes
- Nenhuma dependência de framework específico (Spring, etc.)

Este módulo representa o coração da aplicação e deve ser mantido livre de dependências externas para garantir a portabilidade e facilitar os testes.