# 004 - Superfície de entrega e estudo

## Commit

`feat: complete the portfolio delivery surface`

## Objetivo

Entregar um laboratório reproduzível, estudável e verificável por interface, CI e artefatos equivalentes à AWS.

## Implementacao

- Adiciona painel Angular 22.1 tipado, responsivo e conectado apenas à API real.
- Empacota frontend com Nginx e integra-o ao Compose.
- Prepara CI local para backend, frontend, Compose e rastreabilidade; publicação depende do escopo GitHub `workflow`.
- Documenta execução, decisões, troubleshooting e limites de produção.
- Separa profiles Lambda para API HTTP e consumidor SQS, sem interferir no runtime JVM local.

## Rastreabilidade ADR

ADR aplicado: ADR-0001 - Processamento at-least-once idempotente.

## Verificacao

- `npm --prefix frontend run test:ci` com Node 24 — 1 teste aprovado.
- `npm --prefix frontend run build` com Node 24 — build Angular aprovado.
- `./scripts/package-lambdas.sh` — gerou pacotes independentes de API e worker.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@25 ./mvnw -q clean verify` — 7 testes aprovados, incluindo contrato HTTP e validação em Problem Details.
- `npm --prefix frontend ci && npm --prefix frontend run test:ci && npm --prefix frontend run build` com Node 24 — instalação reproduzível, 1 teste e build aprovados; npm reportou 3 vulnerabilidades moderadas em dependências de desenvolvimento.
- `docker compose config --quiet` — aprovado.
- `./scripts/audit-history.sh` — aprovado após corrigir a auditoria do commit raiz com `git diff-tree --root`.
- `npm --prefix frontend audit --omit=dev` — nenhuma vulnerabilidade de runtime encontrada.
- `docker build -t java-aws-serverless-messaging-lab-frontend:test frontend` — imagem Angular/Nginx aprovada.
- Primeiro `git push` do incremento — recusado pelo GitHub porque o token OAuth não possui escopo `workflow`; YAML preservado localmente fora do commit publicado.

## Alternativas e trade-offs

Uma única Lambda com scheduler não representa a execução AWS: os profiles separados preservam API Gateway e event source mapping como unidades de escala independentes.

## Proximo passo

Executar revisão final, gate completo e conferir CI publicado.
