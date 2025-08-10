Open Finance Use Cases Module
Este módulo implementa os casos de uso (use cases) da aplicação Open Finance, seguindo os princípios da arquitetura hexagonal e as especificações do Open Finance Brasil.
Estrutura do Módulo
Casos de Uso - Accounts (/accounts)
IGetAccountsUseCase
Interface que define o contrato para obtenção da lista de contas do cliente.
Responsabilidades:

Coordenação entre serviços
Logging completo da operação
Medição de performance e SLA
Tratamento centralizado de exceções
Validação de entrada unificada

Input/Output Objects
GetAccountsInput
Record com todos os parâmetros necessários para listagem de contas.
Atributos:

consentId - Identificador do consentimento
organizationId - Identificador da organização receptora
accountType - Filtro opcional de tipo de conta
page - Número da página (mínimo 1)
pageSize - Tamanho da página (1-1000)
paginationKey - Chave para controle de limites operacionais
xFapiInteractionId - UUID obrigatório para correlação
xFapiAuthDate - Data de autenticação FAPI
xFapiCustomerIpAddress - IP do cliente
xCustomerUserAgent - User agent do cliente

GetAccountsOutput
Record com a resposta completa da operação.
Estrutura:
javaGetAccountsOutput {
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
Configuração
AccountsUseCaseConfiguration
Configuração Spring para beans e propriedades dos use cases.
Propriedades Configuráveis:
yamlopen-finance:
api:
base-url: https://api.banco.com.br
pagination:
default-page-size: 25
max-page-size: 1000
key-expiration-minutes: 60
Tratamento de Exceções
UseCaseExceptionHandler
Handler centralizado para exceções do layer de use cases.
Tipos de Exceção Tratados:

DomainException - Exceções do domínio
BusinessRuleViolationException - Violações de regra de negócio
Rate limit exceeded (429)
Consent validation failed (401)
Permission denied (403)
Unexpected errors (500)

Especificações Open Finance Implementadas
Paginação

Padrão: Segue especificação Open Finance Brasil
Parâmetros: page (1-based), page-size (1-1000)
Links HATEOAS: self, first, prev, next, last
Pagination Key: Para controle de limites operacionais
Meta: totalRecords, totalPages, requestDateTime

Limites de Tráfego

TPM (Transações por Minuto): Por origem (IP/organizationId)
TPS (Transações por Segundo): Global mínimo 300 TPS
Categorias: Alta, Média-Alta, Média, Baixa frequência
HTTP Codes: 429 para TPM, 529 para TPS

Limites Operacionais

Periodicidade: Mensal por endpoint/cliente/organização
Não Contabilização: Paginação com chave válida
HTTP Code: 423 para limite excedido
Tracking: Por consentimento, recurso, cliente, organização

Performance (SLA)

Alta Frequência: ≤ 1.500ms (percentil 95)
Média-Alta: ≤ 1.500ms (percentil 95)
Média: ≤ 2.000ms (percentil 95)
Baixa: ≤ 4.000ms (percentil 95)

Disponibilidade

Diária: 95% em 24 horas
Trimestral: 99,5% em 3 meses
Timeout: 15 segundos (504 Gateway Timeout)

Headers FAPI

x-fapi-interaction-id: UUID obrigatório para correlação
x-fapi-auth-date: Data de autenticação (opcional)
x-fapi-customer-ip-address: IP do cliente (opcional)
x-customer-user-agent: User agent (opcional)

Permissões

ACCOUNTS_READ: Obrigatória para endpoint /accounts
Validação através do consentId
Status AUTHORISED necessário

Testes
Testes Unitários

JUnit 5 para estrutura de testes
Mockito para mocks de dependências
AssertJ para assertions fluentes
Cobertura: Todas as regras de negócio

Casos de Teste Principais

Validação de input com parâmetros válidos/inválidos
Rate limiting e limites operacionais
Validação de consentimento e permissões
Paginação e filtros
Tratamento de erros
Performance e SLA

Logs Estruturados
Formato de Log
OPERATION_TYPE - Key1: Value1, Key2: Value2, ..., Timestamp: ISO_DATETIME
Exemplos de Logs
ACCOUNT_ACCESS_START - ConsentId: consent-123, OrganizationId: org-456, Operation: GET_ACCOUNTS, Page: 1, PageSize: 25, AccountTypeFilter: CONTA_DEPOSITO_A_VISTA, InteractionId: uuid, Timestamp: 2024-01-15T10:30:00

ACCOUNT_ACCESS_SUCCESS - ConsentId: consent-123, OrganizationId: org-456, Operation: GET_ACCOUNTS, AccountsReturned: 5, ExecutionTimeMs: 850, InteractionId: uuid, Timestamp: 2024-01-15T10:30:01

RATE_LIMIT_CHECK - OrganizationId: org-456, Status: WITHIN_LIMITS, RemainingRequests: 1250, Timestamp: 2024-01-15T10:30:00
Métricas e Monitoramento
Métricas Coletadas

Request Metrics: Total, errors, avg response time, success rate
SLA Metrics: Compliance rate, violations, avg response time
Pagination Metrics: Usage with/without keys, validation rates
Rate Limit Metrics: Current usage, remaining requests

Compliance Monitoring

SLA compliance rate (target: ≥95%)
Response time percentiles
Error rates por endpoint
Rate limit violations

Arquitetura
Princípios Aplicados

Hexagonal Architecture: Portas e adaptadores
SOLID: Responsabilidade única, inversão de dependência
DDD: Eventos de domínio, serviços especializados
12 Factor App: Configuração, logs, processos stateless

Padrões Utilizados

Builder Pattern: Para objetos complexos
Facade Pattern: Interface simplificada
Strategy Pattern: Para diferentes validações
Observer Pattern: Para eventos de domínio

Dependências
Principais

Lombok: Redução de boilerplate
SLF4J: API de logging
Spring Core: Injeção de dependência
Jakarta Validation: Validações

Testes

JUnit 5: Framework de testes
Mockito: Mocks e stubs
AssertJ: Assertions expressivas

Configuração Maven
xml<dependency>
<groupId>com.openfinance</groupId>
<artifactId>open-finance-core-domain</artifactId>
<version>${project.version}</version>
</dependency>
Próximos Passos

Implementar demais endpoints:

/accounts/{accountId} - Identificação da conta
/accounts/{accountId}/balances - Saldos
/accounts/{accountId}/transactions - Transações
/accounts/{accountId}/transactions-current - Transações recentes
/accounts/{accountId}/overdraft-limits - Limites


Integração com adapters:

API REST (Spring WebFlux)
Persistência (MongoDB)
Segurança (OAuth2/JWT)
Integrações externas (WebClient)


Melhorias:

Cache para otimização
Circuit breakers para resiliência
Observabilidade completa
Testes de integração




Validar consentimento e permissões
Controlar limites de tráfego (TPM/TPS)
Aplicar filtros de tipo de conta
Implementar paginação conforme especificação
Gerenciar chaves de paginação para limites operacionais

GetAccountsUseCaseImpl
Implementação completa do caso de uso para listagem de contas.
Fluxo de Execução:

Validação de Rate Limits - Verifica TPM/TPS
Validação de Consentimento - Status AUTHORISED
Validação de Permissões - ACCOUNTS_READ
Validação de Pagination Key - Para limites operacionais
Busca Externa - Integração com sistema legado
Processamento de Resposta - Paginação e links HATEOAS
Registro de Métricas - Para compliance
Publicação de Eventos - Para auditoria

Serviços de Suporte
AccountsBusinessService
Serviço com regras de negócio específicas para operações de contas.
Funcionalidades:

Validação de parâmetros de entrada
Aplicação de filtros de tipo de conta
Validação de headers FAPI obrigatórios
Cálculo de page size efetivo
Processamento e validação de lista de contas

PaginationLinkBuilderService
Construção de links de paginação seguindo padrões HATEOAS.
Responsabilidades:

Geração de links self, first, prev, next, last
Inclusão de parâmetros de query apropriados
Validação de URLs base
Encoding correto de parâmetros

AccountAccessLoggingService
Logging estruturado para auditoria e compliance.
Tipos de Log:

ACCOUNT_ACCESS_START - Início da operação
ACCOUNT_ACCESS_SUCCESS - Sucesso com métricas
ACCOUNT_ACCESS_FAILURE - Falhas com códigos de erro
RATE_LIMIT_CHECK - Validações de limite
CONSENT_VALIDATION - Validações de consentimento
PERFORMANCE_METRICS - Métricas de performance
COMPLIANCE_METRICS - Conformidade com SLAs

RateLimitValidationService
Controle de limites de tráfego TPM e TPS.
Categorias de Frequência:

Alta Frequência: 2.500+ TPM (baseado em consentimentos)
Média-Alta: 2.000 TPM
Média: 1.500 TPM
Baixa: 1.000 TPM

Limites Globais:

Mínimo 300 TPS simultâneos
Expansão automática em incrementos de 150 TPS

OperationalLimitsService
Controle de limites operacionais mensais.
Limites por Categoria:

Baixa Frequência: 8 chamadas/mês
Média Frequência: 30 chamadas/mês
Média-Alta: 120 chamadas/mês
Alta Frequência: 240 chamadas/mês
Especial (Saldos/Limites): 420 chamadas/mês

Critérios de Contabilização:

Por mês, endpoint, consentimento, cliente e organização
Apenas respostas 2XX são contabilizadas
Chamadas de paginação não são contabilizadas

AccountsMetricsService
Coleta e gestão de métricas operacionais.
Métricas Coletadas:

Contador de requisições por operação/organização
Tempos de resposta médios
Taxa de erro e taxa de sucesso
Métricas de compliance SLA
Métricas de paginação

Facades
GetAccountsFacade
Facade que simplifica a interface e gerencia cross-cutting concerns.
**Responsabilidades