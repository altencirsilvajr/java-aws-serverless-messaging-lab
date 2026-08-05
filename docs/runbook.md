# Runbook local

## Recursos não ficam prontos

```bash
docker compose logs localstack
docker compose exec -T localstack awslocal sqs list-queues
docker compose exec -T localstack awslocal dynamodb list-tables
```

O init hook é idempotente. Para reiniciar um ambiente descartável: `docker compose down` e `docker compose up --build`.

## Mensagem não conclui

Consulte `GET /api/commands/{messageId}`, logs estruturados da API e `GET /api/commands/operations/queues`. `FAILED` com tentativas crescentes é esperado antes do redrive; `COMPLETED` não deve voltar a processamento.

## Reprocessamento da DLQ

Este laboratório não automatiza redrive da DLQ. Em produção, diagnostique a causa, corrija-a e use uma operação autenticada e auditada para reenviar; copiar mensagens cegamente recria o incidente.

## Limpeza

`docker compose down` remove os containers e a rede. O Compose não monta volume persistente para o LocalStack, portanto o estado local é descartável.
