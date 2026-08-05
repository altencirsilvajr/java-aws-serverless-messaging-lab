# Visão do projeto

## Problema de aprendizagem

“Usar SQS” não demonstra senioridade por si só. Este laboratório torna explícitas as decisões que aparecem quando uma mensagem pode ser entregue mais de uma vez: estado persistido, concorrência condicional, retries, DLQ, correlação e evidência operacional.

## Resultado esperado

Uma pessoa deve conseguir subir tudo localmente, publicar um comando pela interface, observar `ACCEPTED → PROCESSING → COMPLETED`, publicar uma duplicata e induzir uma falha que chega à DLQ após três tentativas.

## Limites

- Não realiza deploy nem cria custos em uma conta AWS.
- Não simula garantia exactly-once.
- Não transforma o painel Angular em fonte de regras de negócio.
- O template SAM documenta a topologia equivalente; segurança, alarmes e pipeline de promoção precisam ser completados antes de produção.
