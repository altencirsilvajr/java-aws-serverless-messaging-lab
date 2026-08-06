# 006 - Endurecer toolchain de CI

## Commit

`ci: eliminate toolchain warnings`

## Objetivo

Remover os alertas de seguranca do frontend e os avisos de runtime obsoleto das Actions.

## Implementacao

- Fixa `@hono/node-server` em 2.1.0, acima da versao corrigida 2.0.5.
- Aprova explicitamente scripts de instalacao versionados do toolchain Angular.
- Migra checkout/setup-java/setup-node para Node 24 e adiciona audit ao CI.

## Rastreabilidade ADR

Decisao local sem ADR novo: manutencao reversivel de supply chain, sem alterar contratos do laboratorio.

## Verificacao

- `npm audit`: 0 vulnerabilidades e nenhum script pendente.
- Teste frontend: 1 aprovado; build Angular aprovado.
- Workflow validado como YAML e sem Actions antigas.
