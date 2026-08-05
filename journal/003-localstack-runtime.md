# 003 - Runtime local compatível com AWS

## Commit

`feat: add local AWS runtime`

## Objetivo

Executar o vertical real com SQS, DynamoDB, retries e DLQ sem conta cloud.

## Implementacao

- Provisiona filas, redrive policy e tabela de forma idempotente no LocalStack.
- Empacota a aplicação em imagem Java 25 sem usuário root.
- Orquestra API e dependências com health check no Docker Compose.
- Automatiza smoke de conclusão, duplicata de transporte e falha definitiva.

## Rastreabilidade ADR

ADR aplicado: ADR-0001 - Processamento at-least-once idempotente.

## Verificacao

- `docker compose config --quiet` — aprovado.
- `API_URL=http://localhost:8095 ./scripts/smoke-local.sh` — aprovado para conclusão, duplicata de transporte, três tentativas e DLQ.
- `docker build -t java-aws-serverless-messaging-lab:test .` — aprovado com imagem Java 25.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@25 ./mvnw -q clean package -Plambda -DskipTests` — aprovado e gerou `target/function.zip`.

## Alternativas e trade-offs

O bootstrap usa init hook oficial do LocalStack e URLs configuráveis; isso mantém o ambiente local simples e o SDK idêntico ao usado contra AWS real.

## Proximo passo

Adicionar painel Angular e superfícies de entrega/estudo.
