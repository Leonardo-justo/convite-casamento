# Runbook operacional

Comandos abaixo são para o laboratório e devem ser executados na raiz.

## Saúde básica

```powershell
docker compose ps
Invoke-RestMethod "http://localhost:8081/actuator/health"
Invoke-RestMethod "http://localhost:8082/actuator/health"
Invoke-RestMethod "http://localhost:8081/internal/outbox/status"
Invoke-RestMethod "http://localhost:8082/internal/sync/status"
```

O health do edge pode mostrar Kafka indisponível enquanto `/api/local/products` continua atendendo. Isso é esperado no modo offline; readiness para receber tráfego local e conectividade de sincronização não devem ser a mesma coisa em produção.

## Tópicos

Listar:

```powershell
docker compose exec kafka `
  /opt/kafka/bin/kafka-topics.sh `
  --bootstrap-server localhost:9092 `
  --list
```

Descrever o tópico:

```powershell
docker compose exec kafka `
  /opt/kafka/bin/kafka-topics.sh `
  --bootstrap-server localhost:9092 `
  --describe `
  --topic restaurant.catalog.product.v1
```

O tópico principal e a DLT precisam ter o mesmo número de partições porque o recoverer preserva a partição original.

## Lag do consumidor

```powershell
docker compose exec kafka `
  /opt/kafka/bin/kafka-consumer-groups.sh `
  --bootstrap-server localhost:9092 `
  --describe `
  --group edge-sync-store-001
```

Interpretação:

- `CURRENT-OFFSET`: próximo offset do grupo;
- `LOG-END-OFFSET`: fim atual da partição;
- `LAG`: eventos ainda não confirmados.

Lag zero não prova consistência se um evento foi enviado à DLT. Monitore ambos.

## Inspecionar DLT

```powershell
docker compose exec kafka `
  /opt/kafka/bin/kafka-console-consumer.sh `
  --bootstrap-server localhost:9092 `
  --topic restaurant.catalog.product.v1.dlt `
  --from-beginning `
  --property print.headers=true `
  --property print.key=true
```

Procedimento:

1. identifique exceção, contrato, chave, partição e offset originais;
2. corrija consumidor ou dado;
3. valide em homologação;
4. republique para o tópico principal mantendo `eventId`;
5. confirme aplicação no inbox/projeção;
6. registre auditoria e causa raiz.

Não apague a DLT para “zerar o alerta”.

## Outbox parado

Sintomas:

- `pendingEvents` cresce;
- idade do evento mais antigo cresce;
- Kafka pode estar saudável para outros serviços.

Verifique:

1. DNS/rede e `KAFKA_BOOTSTRAP_SERVERS`;
2. autenticação/ACL em produção;
3. tamanho máximo do record;
4. serialização;
5. disponibilidade das réplicas;
6. logs do `OutboxPublisher`.

O endpoint atual expõe somente contagem. Produção deve expor também idade do mais antigo, tentativas e taxa de drenagem, sem retornar payload sensível.

## Loja ficou offline além da retenção

Não faça reset cego de offsets.

1. marque o domínio da loja como `RESYNC_REQUIRED`;
2. pause a projeção ou escreva em staging;
3. faça nova carga com watermark;
4. aplique deltas posteriores;
5. valide checksum/contagem;
6. promova staging;
7. reabra o domínio para os PDVs.

A retenção de cada tópico deve ser maior que o SLA máximo de desconexão mais margem operacional. Se não for economicamente viável, a recarga automática faz parte do desenho normal.

## Replay controlado

A opção mais segura para auditoria é criar um novo `group.id`, escrever numa projeção temporária e comparar o resultado. Alterar offset do grupo de produção muda estado compartilhado e exige janela, backup e aprovação.

Antes de replay:

- confirme se o handler é idempotente;
- preserve `eventId`;
- conheça o impacto de eventos antigos;
- impeça efeitos externos repetidos, como cobrança ou emissão fiscal;
- registre intervalo de tópicos/partições/offsets.

## Alertas iniciais sugeridos

Os valores finais dependem do negócio:

- outbox mais antigo acima de 60 segundos;
- lag de catálogo acima de 5 minutos;
- qualquer DLT em pagamento/fiscal;
- DLT de referência acima de um pequeno limiar;
- edge sem heartbeat acima do período esperado;
- disco local acima de 80%;
- carga inicial falhando repetidamente;
- loja aproximando-se do limite de retenção.

Para preço/cardápio, alinhe alerta ao tempo de atualização prometido ao cliente. Para pagamento e fiscal, use SLO e plantão próprios.

