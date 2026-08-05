# ADR-0001 - Processamento at-least-once idempotente

## Status

Aceito

## Contexto

SQS entrega mensagens pelo menos uma vez. Falhas depois de um efeito e antes do delete podem produzir nova entrega, e uma demonstração de entrevista precisa tornar essa condição observável em vez de assumir entrega única.

## Decisao

DynamoDB é a fonte de verdade por `messageId`. A criação condicional impede publicação duplicada pelo HTTP. O consumidor atualiza tentativas e estado, reconhece mensagens já concluídas sem repetir o trabalho e deixa falhas sem delete para o redrive nativo do SQS. Depois de três recebimentos, a política da fila move a mensagem para a DLQ.

O mesmo endpoint Jakarta REST pode rodar como serviço JVM local ou atrás de API Gateway/Lambda pelo adaptador Quarkus Lambda HTTP. O laboratório não executa deploy cloud.

## Consequencias

- Duplicatas HTTP e duplicatas de transporte têm evidência distinta.
- Falhas transitórias aumentam `attempts`; falhas persistentes chegam à DLQ.
- DynamoDB e SQS não formam uma transação distribuída; a escrita `ACCEPTED` anterior à publicação pode exigir reconciliação em um produto real.
- O adapter AWS usa SDK oficial, preservando a equivalência entre LocalStack e AWS real.

## Alternativas rejeitadas

### Fila em memória

Não demonstra visibility timeout, redrive policy nem diferenças operacionais entre fila principal e DLQ.

### Exatamente uma vez

Não é uma garantia fornecida pelo SQS standard e esconderia o problema central do laboratório.
