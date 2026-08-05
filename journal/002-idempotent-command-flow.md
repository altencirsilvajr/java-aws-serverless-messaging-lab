# 002 - Fluxo de comando idempotente

## Commit

`feat: implement idempotent command flow`

## Objetivo

Aceitar comandos pela API, processá-los pelo consumidor e tornar estados, tentativas, duplicatas e falhas observáveis.

## Implementacao

- Modela estados e transições do processamento.
- Separa aplicação de adaptadores SQS e DynamoDB por ports reais.
- Implementa API Jakarta REST, consumidor agendado, Problem Details, correlation ID, health, métricas e OpenAPI.
- Habilita o mesmo contrato HTTP para execução convencional ou API Gateway/Lambda.

## Rastreabilidade ADR

Novo ADR criado: ADR-0001 - Processamento at-least-once idempotente.

## Verificacao

- `JAVA_HOME=/opt/homebrew/opt/openjdk@25 ./mvnw -q test` antes da implementação — falhou na compilação porque os tipos de domínio e aplicação ainda não existiam (red esperado).
- `JAVA_HOME=/opt/homebrew/opt/openjdk@25 ./mvnw -q test` após a implementação — aprovado com 5 testes.

## Alternativas e trade-offs

Testes de aplicação usam adapters determinísticos para exercitar as decisões sem depender de Docker; o fluxo AWS real será validado no próximo incremento com LocalStack.

## Proximo passo

Provisionar recursos locais e validar publicação, processamento, retries e DLQ com serviços reais.
