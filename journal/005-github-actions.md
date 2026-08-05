# 005 - Publicar pipeline GitHub Actions

## Commit

`ci: publish github actions workflow`

## Objetivo

Executar os gates do laboratorio serverless automaticamente no host publico.

## Implementacao

- Adiciona pipeline para Java 25, Quarkus, Maven, Angular e Node 24.
- Audita todo o historico e valida backend, frontend e Compose.
- Nao injeta credenciais AWS no fluxo de verificacao.

## Rastreabilidade ADR

Decisao local sem ADR novo: o pipeline automatiza os gates existentes sem alterar a arquitetura.

## Verificacao

- Ruby/Psych carregou o YAML sem erro de sintaxe.
- `./mvnw verify`: aprovado com 7 testes.
- `npm --prefix frontend ci`, teste CI e build: aprovados com 1 teste.
- `./scripts/audit-history.sh`: aprovado antes da publicacao.

## Alternativas e trade-offs

A execucao usa adapters locais e nao depende de uma conta AWS real.

## Proximo passo

Acompanhar a primeira execucao publica do pipeline.
