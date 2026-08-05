# Processo de desenvolvimento

Este laboratório evolui em incrementos verticais pequenos, verificáveis e reversíveis.

Cada commit substantivo deve:

1. entregar um único resultado observável;
2. conter exatamente um registro em `journal/` criado ou atualizado;
3. registrar no journal os comandos realmente executados;
4. passar pelo gate `./scripts/verify-traceability.sh --staged`;
5. criar ADR somente para decisões duráveis e difíceis de reverter.

O histórico Git é parte do material de estudo: não deve ser reescrito depois de publicado.
