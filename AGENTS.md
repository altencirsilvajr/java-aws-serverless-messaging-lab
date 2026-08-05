# AGENTS.md

## Escopo

Estas instruções valem para todo o repositório.

## Processo

- Trabalhe em incrementos verticais, testáveis e atômicos.
- Cada commit substantivo não merge deve adicionar ou atualizar exatamente um arquivo em `journal/`.
- Registre decisões duráveis em `docs/adr/` e mantenha `docs/sdd/active.md` atual.
- Use inglês em código, identificadores e commits; documentação em português brasileiro.
- Não versione segredos nem registre verificações que não foram executadas.

## Verificação obrigatória

Antes de cada commit substantivo:

```bash
./scripts/verify-traceability.sh --staged
git diff --check --cached
```

Depois do bootstrap da aplicação:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@25 ./mvnw verify
PATH=/opt/homebrew/opt/node@24/bin:$PATH npm --prefix frontend ci
PATH=/opt/homebrew/opt/node@24/bin:$PATH npm --prefix frontend run test:ci
PATH=/opt/homebrew/opt/node@24/bin:$PATH npm --prefix frontend run build
docker compose config --quiet
```
