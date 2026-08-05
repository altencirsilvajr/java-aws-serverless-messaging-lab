# Guia de estudo

## Roteiro de demonstração

1. Abra o painel e publique um comando normal; explique por que o POST retorna antes do trabalho.
2. Aguarde `COMPLETED` e mostre `attempts = 1` no DynamoDB por meio da API.
3. Publique uma duplicata de transporte; mostre `duplicateCount` sem novo efeito.
4. Ative a falha persistente; observe três tentativas, `FAILED` e a contagem da DLQ.
5. Abra métricas, health e OpenAPI para mostrar operabilidade e contrato.

## Perguntas de entrevista

**Onde está a idempotência?** Na chave `messageId`, na criação condicional e na decisão do consumidor ao encontrar um registro concluído.

**Por que não exactly-once?** Porque seria uma promessa maior que a garantia do SQS Standard. Persistir a decisão torna o comportamento repetível.

**Qual lacuna transacional permanece?** DynamoDB é escrito antes do `SendMessage`; uma falha entre ambos pode deixar um comando aceito sem publicação. Produção exigiria reconciliação, outbox ou mudança de fronteira.

**Como a DLQ ajuda?** Ela limita retries infinitos e separa mensagens que exigem diagnóstico/reprocessamento controlado.

**O que muda na AWS?** Retira-se endpoint override, entram IAM roles e endpoints reais, e o polling JVM é substituído pelo event source mapping SQS → Lambda.
