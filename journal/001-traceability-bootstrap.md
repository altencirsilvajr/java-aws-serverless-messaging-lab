# 001 - Bootstrap de rastreabilidade

## Commit

`chore: bootstrap tracked development`

## Objetivo

Estabelecer o processo auditável antes do primeiro incremento funcional.

## Implementacao

- Define instruções do repositório, SDD ativo e convenções de desenvolvimento.
- Adiciona gate executável para exigir exatamente um journal por commit.
- Adiciona auditoria do histórico não merge.

## Rastreabilidade ADR

Decisao local sem ADR novo: o processo é uma exigência operacional do laboratório e não altera sua arquitetura de runtime.

## Verificacao

- `./scripts/verify-traceability.sh --staged` — aprovado com exatamente um journal staged.
- `git diff --check --cached` — aprovado sem erros de whitespace.

## Alternativas e trade-offs

Um único documento sem gate seria mais curto, porém não impediria lacunas futuras no histórico.

## Proximo passo

Implementar o domínio e o caso de uso no primeiro vertical test-first.
