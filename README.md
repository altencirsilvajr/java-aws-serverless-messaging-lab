# Java AWS Serverless Messaging Lab

Laboratório vertical Java 25 para entrevistas Senior: Jakarta REST/Quarkus, SQS, consumidor idempotente, DynamoDB, retries e DLQ, executados localmente com LocalStack e visualizados por Angular 22.1.

## Fluxo

```text
Angular :8088 → HTTP API :8080 → SQS processing-commands
                                      ↓ at-least-once
                              consumidor Quarkus
                                      ↓
                              DynamoDB processing-state
                                      ↓ falha após 3 recebimentos
                              SQS processing-commands-dlq
```

A criação condicional no DynamoDB impede que o mesmo `messageId` seja publicado duas vezes pelo HTTP. No transporte, uma mensagem já `COMPLETED` é reconhecida sem repetir o trabalho e incrementa `duplicateCount`. Falhas não são deletadas: o visibility timeout permite nova tentativa e a redrive policy move a mensagem para a DLQ.

## Executar

Requisitos: Docker Desktop. O caminho principal não exige conta AWS nem credenciais reais.

```bash
docker compose up --build
```

- Painel: http://localhost:8088
- OpenAPI: http://localhost:8080/q/openapi
- Swagger UI: http://localhost:8080/q/swagger-ui
- Health: http://localhost:8080/q/health
- Métricas: http://localhost:8080/q/metrics

Para executar backend e frontend fora de containers:

```bash
docker compose up -d localstack
JAVA_HOME=/opt/homebrew/opt/openjdk@25 AWS_ENDPOINT=http://localhost:4566 AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test ./mvnw quarkus:dev
PATH=/opt/homebrew/opt/node@24/bin:$PATH npm --prefix frontend start
```

## Verificar

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@25 ./mvnw verify
PATH=/opt/homebrew/opt/node@24/bin:$PATH npm --prefix frontend ci
PATH=/opt/homebrew/opt/node@24/bin:$PATH npm --prefix frontend run test:ci
PATH=/opt/homebrew/opt/node@24/bin:$PATH npm --prefix frontend run build
docker compose config --quiet
./scripts/smoke-local.sh
./scripts/audit-history.sh
```

## Equivalência com AWS real

O runtime local e a AWS real usam os mesmos clientes AWS SDK v2 e os mesmos contratos. `AWS_ENDPOINT` só existe no LocalStack; sem ele, o SDK usa endpoints regionais e a cadeia padrão de credenciais/IAM. O template `infra/aws/template.yaml` descreve API Gateway/Lambda, SQS com event source mapping, DynamoDB e DLQ, mas não é implantado por este repositório.

Os pacotes Lambda são gerados explicitamente:

```bash
./scripts/package-lambdas.sh
# target/lambda/api.zip
# target/lambda/worker.zip
```

Antes de produção ainda seriam necessários least privilege refinado, KMS, CloudWatch alarms, tracing distribuído, partial batch response, reconciliação de comandos aceitos mas não publicados e estratégia de deploy/rollback.

## Decisões defendíveis em entrevista

- SQS Standard oferece entrega *at-least-once*; idempotência é uma responsabilidade do consumidor.
- DynamoDB guarda um registro pequeno por chave e usa conditional writes para coordenar concorrência sem lock global.
- A falha simulada não deleta a mensagem, preservando a semântica real de visibility timeout e redrive.
- A API retorna `202 Accepted`; processamento e resultado são consultados separadamente.
- O perfil JVM usa polling para aprendizado local; na AWS, o event source mapping invoca uma Lambda dedicada.

Comece por [PROJECT_VISION.md](PROJECT_VISION.md), [ADR-0001](docs/adr/ADR-0001-at-least-once-processing.md) e [guia de estudo](docs/study-guide.md).
