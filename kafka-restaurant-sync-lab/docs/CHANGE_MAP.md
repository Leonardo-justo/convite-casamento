# Mapa exato de alterações

Este arquivo responde à pergunta “em qual lugar eu mexo?”.

## Configurações mais frequentes

| Necessidade | Arquivo/propriedade | Observação |
|---|---|---|
| Versão do Spring Boot/Java | `pom.xml` | Propriedades `spring-boot.version` e `java.version` |
| Versão do broker local | `compose.yaml` | Imagem `apache/kafka` |
| URL/usuário do banco central | `servico-central-catalogo/src/main/resources/application.yml` | `CENTRAL_DB_URL`, `CENTRAL_DB_USER`, `CENTRAL_DB_PASSWORD` |
| Endereço dos brokers | os dois `application.yml` | `KAFKA_BOOTSTRAP_SERVERS`; em produção inclua segurança |
| Nome do tópico | os dois `application.yml` | `PRODUCT_TOPIC`; produtor e consumidor devem apontar para o mesmo nome |
| Partições/réplicas | `servico-central-catalogo/.../KafkaTopicConfiguration.java` | Em produção provisione por IaC; o concentrador não deve ter permissão administrativa |
| Identidade da loja | `servico-concentrador-loja/application.yml` | `TENANT_ID`, `STORE_ID` e `EDGE_CONSUMER_GROUP` |
| Banco local | `servico-concentrador-loja/application.yml` | `EDGE_DB_PATH`; use caminho persistente e com backup |
| Quantidade de consumidores | `servico-concentrador-loja/application.yml` | `KAFKA_CONSUMER_CONCURRENCY`, no máximo útil igual ao total de partições atribuídas |
| Retry e DLT | `KafkaConsumerConfiguration.java` e `application.yml` do edge | `KAFKA_RETRY_DELAY_MS` e `KAFKA_MAX_RETRIES` |
| Frequência do outbox | `servico-central-catalogo/application.yml` | `OUTBOX_FIXED_DELAY` e `OUTBOX_SEND_TIMEOUT` |
| URL da carga inicial | `servico-concentrador-loja/application.yml` | `CATALOG_BASE_URL` |

## Alterar o contrato do evento

Arquivos:

- `contratos-eventos/.../ProductChangedEvent.java`: envelope;
- `contratos-eventos/.../ProductPayload.java`: dados do produto;
- `contratos-eventos/.../EventTypes.java`: tipos semânticos;
- `docs/EVENT_CONTRACT.md`: documentação para outros times.

Para adicionar um campo opcional:

1. adicione o campo ao final do payload;
2. mantenha o consumidor tolerante a campos desconhecidos;
3. publique primeiro consumidores compatíveis;
4. depois publique o produtor;
5. acrescente teste de compatibilidade.

Para remover, renomear ou mudar o significado de um campo, crie `schemaVersion = 2` e, normalmente, um tópico `.v2`. Não troque o contrato de forma destrutiva no mesmo tópico.

## Alterar a produção do evento

Fluxo:

1. `ProductController` recebe o comando HTTP.
2. `ProductService` altera `catalog_product` e insere `outbox_event` dentro do mesmo `@Transactional`.
3. `OutboxPublisher` entrega o JSON ao Kafka.
4. `OutboxPublicationService` marca `published_at`.

Se uma nova forma de alterar produto for criada (batch, ERP, importação, monólito), ela também precisa gerar o outbox. Uma escrita direta na tabela sem evento deixa as lojas desatualizadas.

O publisher didático consulta os 100 eventos mais antigos. Com várias réplicas em produção, implemente uma destas opções:

- claim de linhas com `FOR UPDATE SKIP LOCKED`;
- status/lease com timeout;
- Debezium Outbox Event Router, deixando o CDC publicar o outbox.

## Alterar o consumo local

Fluxo:

1. `ProductEventConsumer` recebe o record;
2. `ProductEventProcessor` valida versão e loja;
3. consulta `processed_event` para idempotência;
4. compara `sourceVersion` para não regredir;
5. grava `local_product` e `processed_event` na mesma transação;
6. somente após o retorno o offset é confirmado.

Exceções do processor são tratadas por `KafkaConsumerConfiguration`: há três novas tentativas e, depois, publicação em `<topico>.dlt`.

Erros de infraestrutura local, como H2 temporariamente bloqueado, podem se recuperar no retry. Erros permanentes, como contrato inválido, chegam à DLT. Em produção, classifique exceções não retentáveis para não esperar inutilmente.

## Alterar a carga inicial

Arquivos:

- central: endpoint em `ProductController.snapshot`;
- cliente HTTP: `CatalogSnapshotClient`;
- reconciliação local: `SnapshotImportService`;
- disparo: `InitialLoadController`.

A regra mais importante está em `SnapshotImportService`: um snapshot só substitui o registro local se sua versão for maior. Isso impede a seguinte corrida:

```text
snapshot v4 começa → evento Kafka v5 chega → snapshot v4 termina
```

Sem comparação de versão, o preço novo seria sobrescrito pelo antigo.

Para exclusões, não dependa da ausência no snapshot. Use evento explícito de desativação ou um manifesto de reconciliação com marca de corte.

## Criar o segundo domínio

Exemplo: promoção.

1. Defina `PromotionChangedEvent` e seus exemplos.
2. Defina tópico `restaurant.pricing.promotion.v1`.
3. Escolha chave de partição e regra de ordenação.
4. Na API dona do dado, grave alteração + outbox em uma transação.
5. Crie tópico principal e DLT via infraestrutura.
6. No edge, crie projeção `local_promotion`.
7. Crie inbox idempotente ou reutilize a tabela genérica `processed_event`.
8. Crie snapshot paginado e versionado.
9. Teste duplicidade, evento fora de ordem, DLT, reconexão e carga concorrente.
10. Adicione métricas de lag, idade do outbox e última sincronização.

Não copie quinze consumidores antes de criar uma pequena biblioteca interna para envelope, métricas, error handler e inbox. A biblioteca deve padronizar infraestrutura, não compartilhar entidades ou regras de negócio.

## Configurações que não devem ficar fixas no código

- brokers e protocolo (`SASL_SSL`);
- credenciais e certificados;
- identidade de tenant/loja/dispositivo;
- nomes por ambiente;
- URLs centrais;
- caminhos do banco local;
- limites de lote, retry e timeouts;
- feature flags para ativar um domínio durante o rollout.

Use variáveis de ambiente ou uma solução de configuração/segredos. Nunca coloque senha do banco, senha de Kafka ou chave fiscal no repositório.
