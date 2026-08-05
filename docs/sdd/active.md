# SDD ativo — AWS Serverless Messaging Lab

## Objetivo

Demonstrar localmente um fluxo assíncrono compatível com AWS: uma API aceita comandos, publica em SQS, um consumidor idempotente processa mensagens, e DynamoDB registra as transições observáveis até conclusão ou falha definitiva em DLQ.

## Restrições

- Java 25, Quarkus 3.33 LTS e Maven.
- Angular 22.1 como painel funcional mínimo.
- LocalStack para a demonstração; nenhum deploy em conta AWS.
- Código e contratos em inglês; documentação em português brasileiro.
- Testes no seam público e adaptadores determinísticos quando o teste com infraestrutura real for desproporcional.

## Incrementos planejados

1. Bootstrap de rastreabilidade — concluído.
2. Domínio, caso de uso, API e adaptadores AWS — concluído.
3. Infraestrutura LocalStack e teste do fluxo real.
4. Painel Angular, entrega, observabilidade e documentação.
