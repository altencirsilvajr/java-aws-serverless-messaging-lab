# 007 - Reparar lockfile do frontend

## Commit

`fix: synchronize frontend lockfiles`

## Objetivo

Restaurar o lockfile completo e sincroniza-lo com a politica de scripts.

## Implementacao

- Recupera a copia integral preservada na branch de seguranca.
- Regenera o metadata do lockfile com npm 11.17.

## Rastreabilidade ADR

Decisao local sem ADR novo: reparo de artefato de dependencias, sem mudanca funcional.

## Verificacao

- JSON do lockfile valido e `npm ci` aprovado sem warnings.
- `npm audit --audit-level=moderate`: 0 vulnerabilidades.
- Nenhum script de instalacao pendente.
