# Guia de estudo: Kafka + Spring Boot neste laboratório

Este arquivo é o roteiro de leitura do projeto. Leia o fluxo completo uma vez e depois abra cada classe na ordem indicada. Os comentários no código explicam o detalhe local; este guia explica como as peças se encaixam.

## 1. O problema de negócio

Imagine o catálogo central de uma rede de restaurantes. O ERP ou a API central altera um produto. Cada loja possui uma cópia local para continuar vendendo mesmo sem internet. Kafka transporta a mudança quando há conectividade; a API local atende o PDV sem depender do servidor central.

O laboratório possui três módulos:

- `contratos-eventos`: contrato compartilhado dos eventos. Em um sistema real, ele deve ser versionado com cuidado.
- `servico-central-catalogo`: fonte de verdade do catálogo. Grava no PostgreSQL e publica mudanças.
- `servico-concentrador-loja`: sincronizador instalado na loja. Consome Kafka, grava H2 local e expõe endpoints locais.

## 2. Fluxo de uma alteração

```text
POST/PUT central
   -> ProductController
   -> ProductService (@Transactional)
      -> catalog_product
      -> outbox_event       (mesma transação)
   -> OutboxPublisher (polling)
      -> Kafka topic restaurant.catalog.product.v1
   -> ProductEventConsumer
   -> ProductEventProcessor (@Transactional)
      -> processed_event (inbox/idempotência)
      -> local_product
   -> PDV chama /api/local/products
```

O ponto mais importante é o outbox: a requisição não tenta fazer banco e Kafka na mesma transação distribuída. Se Kafka cair, o registro permanece no outbox e será tentado novamente.

## 3. Como estudar Kafka usando este código

1. Leia `TopicNames` e `application.yml` para identificar tópico, grupo, partições e serializers.
2. Leia `ProductService.enqueueEvent`: veja a chave Kafka, o `eventId`, o `schemaVersion` e o `sourceVersion`.
3. Leia `OutboxPublisher`: entenda o produtor assíncrono com confirmação (`send().get(...)`).
4. Leia `KafkaConsumerConfiguration`: entenda retry, backoff, ack e DLT.
5. Leia `ProductEventConsumer`: ele é apenas o adaptador Kafka; a regra fica no processor.
6. Leia `ProductEventProcessor`: veja validação, filtro por loja, deduplicação e proteção contra versão antiga.
7. Leia os testes: cada teste representa uma garantia arquitetural.

## 4. Conceitos que você precisa dominar

### Tópico, partição e chave

Um tópico é o log de eventos. Partições permitem paralelismo. Eventos com a mesma chave vão para a mesma partição e preservam ordem relativa. Aqui a chave é `storeId:productId`, então alterações do mesmo produto permanecem ordenadas.

### Consumer group

Cada loja deve possuir um grupo próprio se todas as lojas precisam receber todos os eventos destinados a elas. Dentro de um grupo, uma mensagem é processada por apenas um consumidor. Se várias instâncias do mesmo sincronizador usam o mesmo grupo, elas dividem as partições (alta disponibilidade, não duplicação de lojas).

### At-least-once

Kafka pode entregar a mesma mensagem novamente. Por isso o consumidor grava `processed_event` e compara `product.version`. “Exatamente uma vez” não deve ser presumido apenas por configurar Kafka; a aplicação precisa ser idempotente.

### Outbox e Inbox

Outbox protege a publicação (banco central -> Kafka). Inbox protege o consumo (Kafka -> banco local). Os dois padrões tornam falhas recuperáveis.

### Snapshot e atualização incremental

O snapshot carrega a situação inicial. Depois dele, eventos incrementais atualizam apenas o que mudou. A versão impede que uma carga antiga sobrescreva uma alteração mais nova recebida em tempo real.

### Retry e DLT

Falhas transitórias são tentadas novamente. Depois do limite, a mensagem vai para `<topico>.dlt`. DLT não é lixo: é uma fila operacional para inspeção, correção e reprocessamento controlado.

## 5. Onde alterar no projeto real

- Novo campo do evento: `contratos-eventos`, produtor, consumidor, migration e testes.
- Novo domínio (preço, estoque, pedido): crie contrato e tópico próprios; não transforme um evento de produto em “evento genérico”.
- Mais lojas: defina claramente tenant/store, grupos e política de retenção.
- Offline bidirecional: este laboratório é somente central -> loja. Para loja -> central, crie comandos com `commandId`, outbox local, confirmação e resolução de conflitos.
- Segurança: adicione autenticação, ACLs Kafka, TLS/SASL e não exponha endpoints internos publicamente.

## 6. Exercícios recomendados

1. Pare o Kafka, crie um produto e consulte `/internal/outbox/status`; depois ligue Kafka e veja o envio acontecer.
2. Publique o mesmo JSON duas vezes e confirme que o segundo resultado é `DUPLICATE`.
3. Publique versão 5 e depois versão 4; a versão 4 deve resultar em `STALE_VERSION`.
4. Altere `schemaVersion` para 2; observe retries e DLT.
5. Troque o `STORE_ID` da instância edge; eventos de outra loja devem ser ignorados.
6. Aumente o número de partições e rode duas instâncias edge com o mesmo group id; observe a divisão de partições.

## 7. Perguntas de arquitetura para levar ao projeto da empresa

Quem é a fonte de verdade? Qual é a chave de ordenação? Qual é a política de retenção? Como reprocessar uma loja? Como detectar atraso (lag)? O que acontece quando duas fontes alteram o mesmo produto? Qual contrato de compatibilidade será adotado? Onde ficam métricas, tracing e alertas? Essas perguntas são tão importantes quanto a configuração do broker.
