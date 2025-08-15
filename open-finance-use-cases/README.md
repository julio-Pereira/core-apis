# Open Finance Use Cases Module

Este módulo implementa os casos de uso (use cases) da aplicação Open Finance, seguindo os princípios da arquitetura hexagonal e as especificações do Open Finance Brasil.

## Estrutura do Módulo

### Interface Base

#### `IUseCase<I, O>`
Interface base para todos os casos de uso da aplicação, seguindo o princípio da responsabilidade única e arquitetura hexagonal.

```java
public interface IUseCase<I, O> {
    O execute(I input);
}
```

**Características:**
- **Genérica**: Suporte a qualquer tipo de entrada (I) e saída (O)
- **Padronizada**: Método `execute()` uniforme para todos os use cases
- **Exception Safety**: Lança `DomainException` quando regras de negócio são violadas

### Interface de Eventos

#### `IEventPublisher`
Interface para publicação de eventos de domínio, seguindo o padrão da arquitetura hexagonal.

```java
public interface IEventPublisher {
    <T extends DomainEvent> void publish(T event);
    <T extends DomainEvent> void publishAll(Iterable<T> events);
}
```

**Funcionalidades:**
- Publicação de eventos únicos ou em lote
- Suporte genérico para qualquer tipo de `DomainEvent`
- Desacoplamento para comunicação externa

## Casos de Uso - Accounts (/accounts)

### `IGetAccountsUseCase`
Interface que define o contrato para obtenção da lista de contas do cliente.

**Responsabilidades:**
- Coordenação entre serviços
- Logging completo da operação
- Medição de performance e SLA
- Tratamento centralizado de exceções
- Validação de entrada unificada

### `GetAccountsFacade`
Implementa o padrão Facade para simplificar o acesso ao caso de uso de listagem de contas.

**Características:**
- Encapsula a complexidade do use case
- Fornece interface simplificada para o adapter
- Mantém baixo acoplamento entre camadas

## Input/Output Objects

### `GetAccountsInput`
Record com todos os parâmetros necessários para listagem de contas.

**Atributos:**
- `consentId` - Identificador do consentimento
- `organizationId` - Identificador da organização receptora
- `accountType` - Filtro opcional de tipo de conta
- `page` - Número da página (mínimo 1)
- `pageSize` - Tamanho da página (1-1000)
- `paginationKey` - Chave para controle de limites operacionais
- `xFapiInteractionId` - UUID obrigatório para correlação
- `xFapiAuthDate` - Data de autenticação FAPI
- `xFapiCustomerIpAddress` - IP do cliente
- `xCustomerUserAgent` - User agent do cliente

### `GetAccountsOutput`
Record com a resposta completa da operação.

**Estrutura:**
```java
GetAccountsOutput {
    List<Account> accounts,
    PaginationInfo paginationInfo,
    LocalDateTime requestDateTime
}

PaginationInfo {
    String selfLink,
    String firstLink,
    String prevLink,
    String nextLink,
    String lastLink,
    int totalRecords,
    int totalPages,
    int currentPage,
    int pageSize,
    String paginationKey
}
```

## Configuração

### `AccountsUseCaseConfiguration`
Configuração Spring para beans e propriedades dos use cases.

**Beans Configurados:**
- `Pagination paginationLinkBuilderService()` - Serviço de construção de links de paginação
- Validação automática de configurações na inicialização

**Propriedades Configuráveis:**
```yaml
open-finance:
  api:
    base-url: https://api.banco.com.br
  pagination:
    default-page-size: 25
    max-page-size: 1000
    key-expiration-minutes: 60
```

**Validações de Configuração:**
- Base URL obrigatória e não vazia
- Page size padrão entre 1 e max-page-size
- Alertas para configurações fora dos padrões recomendados
- Log detalhado das configurações validadas

### Serviços de Suporte

#### `Pagination`
Construção de links de paginação seguindo padrões HATEOAS.

**Responsabilidades:**
- Geração de links self, first, prev, next, last
- Inclusão de parâmetros de query apropriados
- Validação de URLs base
- Encoding correto de parâmetros

## Fluxo de Execução

### `GetAccountsUseCaseImpl`
Implementação completa do caso de uso para listagem de contas.

**Passos do Fluxo:**
1. **Validação de Rate Limits** - Verifica TPM/TPS
2. **Validação de Consentimento** - Status AUTHORISED
3. **Validação de Permissões** - ACCOUNTS_READ
4. **Validação de Pagination Key** - Para limites operacionais
5. **Busca Externa** - Integração com sistema legado
6. **Processamento de Resposta** - Paginação e links HATEOAS
7. **Registro de Métricas** - Para compliance
8. **Publicação de Eventos** - Para auditoria

### Responsabilidades dos Use Cases
- Validar consentimento e permissões
- Controlar limites de tráfego (TPM/TPS)
- Aplicar filtros de tipo de conta
- Implementar paginação conforme especificação
- Gerenciar chaves de paginação para limites operacionais

## Tratamento de Exceções

### `UseCaseExceptionHandler`
Handler centralizado para exceções do layer de use cases.

**Tipos de Exceções Tratadas:**
- `DomainException` - Violações de regras de negócio
- `ValidationException` - Erros de validação de entrada
- `BusinessException` - Erros de lógica de negócio
- `TechnicalException` - Erros técnicos/infraestrutura

## Logs Estruturados

### Formato de Log
```
OPERATION_TYPE - Key1: Value1, Key2: Value2, ..., Timestamp: ISO_DATETIME
```

### Exemplos de Logs
```
ACCOUNT_ACCESS_START - ConsentId: consent-123, OrganizationId: org-456, Operation: GET_ACCOUNTS, Page: 1, PageSize: 25, AccountTypeFilter: CONTA_DEPOSITO_A_VISTA, InteractionId: uuid, Timestamp: 2024-01-15T10:30:00

ACCOUNT_ACCESS_SUCCESS - ConsentId: consent-123, OrganizationId: org-456, Operation: GET_ACCOUNTS, AccountsReturned: 5, ExecutionTimeMs: 850, InteractionId: uuid, Timestamp: 2024-01-15T10:30:01

RATE_LIMIT_CHECK - OrganizationId: org-456, Status: WITHIN_LIMITS, RemainingRequests: 1250, Timestamp: 2024-01-15T10:30:00
```

## Métricas e Monitoramento

### Métricas Coletadas
- **Request Metrics**: Total, errors, avg response time, success rate
- **SLA Metrics**: Compliance rate, violations, avg response time
- **Pagination Metrics**: Usage with/without keys, validation rates
- **Rate Limit Metrics**: Current usage, remaining requests

### Compliance Monitoring
- SLA compliance rate (target: ≥95%)
- Response time percentiles
- Error rates por endpoint
- Rate limit violations

## Testes

### Testes Unitários
- **JUnit 5** para estrutura de testes
- **Mockito** para mocks de dependências
- **AssertJ** para assertions fluentes
- **Cobertura**: Todas as regras de negócio

### Casos de Teste Principais
- Validação de input com parâmetros válidos/inválidos
- Rate limiting e limites operacionais
- Validação de consentimento e permissões
- Paginação e filtros
- Tratamento de erros
- Performance e SLA

## Arquitetura

### Princípios Aplicados
- **Hexagonal Architecture**: Portas e adaptadores
- **SOLID**: Responsabilidade única, inversão de dependência
- **DDD**: Eventos de domínio, serviços especializados
- **12 Factor App**: Configuração, logs, processos stateless

### Padrões Utilizados
- **Builder Pattern**: Para objetos complexos
- **Facade Pattern**: Interface simplificada
- **Strategy Pattern**: Para diferentes validações
- **Observer Pattern**: Para eventos de domínio

## Dependências

### Principais
- **Lombok**: Redução de boilerplate
- **SLF4J**: API de logging
- **Spring Core**: Injeção de dependência
- **Jakarta Validation**: Validações

### Testes
- **JUnit 5**: Framework de testes
- **Mockito**: Mocks e stubs
- **AssertJ**: Assertions expressivas

## Configuração Maven

```xml
<dependency>
    <groupId>com.openfinance</groupId>
    <artifactId>open-finance-core-domain</artifactId>
    <version>${project.version}</version>
</dependency>
```

## Próximos Passos

### Implementar demais endpoints:
- `/accounts/{accountId}` - Identificação da conta
- `/accounts/{accountId}/balances` - Saldos
- `/accounts/{accountId}/transactions` - Transações
- `/accounts/{accountId}/transactions-current` - Transações recentes
- `/accounts/{accountId}/overdraft-limits` - Limites

### Integração com adapters:
- API REST (Spring WebFlux)
- Persistência (MongoDB)
- Segurança (OAuth2/JWT)
- Integrações externas (WebClient)

### Melhorias:
- Cache para otimização
- Circuit breakers para resiliência
- Observabilidade completa
- Testes de integração