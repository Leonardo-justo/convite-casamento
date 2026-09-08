# Contrato e catálogo de eventos

## Evento implementado

Tópico: `restaurant.catalog.product.v1`  
Chave: `<storeId>:<productId>`  
Semântica: estado mais recente do produto para uma loja  
Entrega esperada: at-least-once

```json
{
  "eventId": "44444444-4444-4444-4444-444444444444",
  "schemaVersion": 1,
  "eventType": "ProductUpserted",
  "occurredAt": "2026-07-23T12:00:00Z",
  "source": "servico-central-catalogo",
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "storeId": "22222222-2222-2222-2222-222222222222",
  "data": {
    "productId": "33333333-3333-3333-3333-333333333333",
    "sku": "X-BURGER",
    "name": "X-Burger",
    "price": 24.90,
    "active": true,
    "version": 7
  }
}
```

## Significado dos campos

| Campo | Regra |
|---|---|
| `eventId` | UUID único e estável durante todos os retries/replays |
| `schemaVersion` | Versão do contrato, não a versão do produto |
| `eventType` | Fato de negócio no passado, como `ProductUpserted` |
| `occurredAt` | Instante UTC atribuído pelo sistema central |
| `source` | Serviço que declarou o fato |
| `tenantId` | Empresa/rede; obrigatório para isolamento |
| `storeId` | Loja destinatária |
| `data.productId` | Identificador global e imutável |
| `data.version` | Versão monotônica da entidade usada contra eventos fora de ordem |

Não use o horário da máquina da loja para resolver conflito: relógios de clientes podem estar incorretos.

## Chave e ordenação

Kafka só garante ordem dentro de uma partição. Eventos com a mesma chave chegam à mesma partição; por isso todas as alterações do mesmo produto/loja preservam ordem.

Se uma regra exigir ordem de **todos** os produtos de uma loja, a chave teria de ser somente `storeId`. Isso reduz paralelismo e deve ser justificado. A maioria das projeções funciona com ordem por agregado e proteção por versão.

## Duplicidade e “exactly once”

O fluxo completo envolve PostgreSQL, Kafka e H2. Não existe uma transação única entre os três. A garantia correta é:

- outbox para não perder o evento;
- produtor idempotente para reduzir duplicações de rede;
- entrega at-least-once;
- inbox `processed_event` para absorver duplicações;
- operação de projeção idempotente;
- versão monotônica para absorver ordem antiga.

Exactly-once do Kafka cobre o caso Kafka `read → process → write` quando transações Kafka são usadas. Ele não torna automaticamente uma gravação em banco externo exactly-once.

## Carga inicial

Endpoint implementado:

```text
GET /internal/snapshots/products?tenantId=<uuid>&storeId=<uuid>
```

A resposta inclui `generatedAt` e a mesma `version` usada nos eventos. Para volumes grandes, a versão de produção precisa de:

- paginação por cursor estável;
- compressão;
- checksum e contagem por domínio;
- limite de memória;
- marca de corte global (outbox sequence ou LSN);
- capacidade de retomar páginas;
- manifesto de itens excluídos/desativados;
- status persistido por loja e domínio.

## Catálogo proposto para aproximadamente 15 APIs

Os nomes são uma hipótese inicial e devem ser ajustados ao vocabulário do sistema.

| # | Domínio/API | Fluxo predominante | Evento/tópico sugerido |
|---:|---|---|---|
| 1 | Empresas/tenants | central → loja | `restaurant.identity.tenant.v1` |
| 2 | Lojas | central → loja | `restaurant.store.store.v1` |
| 3 | Operadores e permissões | central → loja | `restaurant.identity.operator.v1` |
| 4 | Produtos/cardápio | central → loja | `restaurant.catalog.product.v1` |
| 5 | Preços/listas | central → loja | `restaurant.pricing.price.v1` |
| 6 | Promoções | central → loja | `restaurant.pricing.promotion.v1` |
| 7 | Estoque/insumos | bidirecional | `restaurant.inventory.movement.v1` |
| 8 | Receitas/fichas técnicas | central → loja | `restaurant.menu.recipe.v1` |
| 9 | Balança/PLU | central → loja | `restaurant.scale.plu.v1` |
| 10 | Configuração de dispositivos | central → loja | `restaurant.device.configuration.v1` |
| 11 | Clientes/fidelidade | bidirecional | `restaurant.customer.profile.v1` |
| 12 | Pedidos/vendas | loja → central + confirmação | `restaurant.sales.order.v1` |
| 13 | Pagamentos | loja → central + confirmação | `restaurant.payment.status.v1` |
| 14 | Fiscal | loja → central + confirmação | `restaurant.fiscal.document.v1` |
| 15 | Caixa/turnos | bidirecional | `restaurant.cashier.shift.v1` |

ERP deve consumir/publicar eventos dos domínios donos, não escrever silenciosamente nas tabelas. Quando isso não for possível durante a transição, trate-o como um produtor legado que também precisa de outbox ou CDC.

## Regras de evolução

- Consumidores ignoram campos desconhecidos.
- Campo novo começa opcional e com significado documentado.
- Não reutilize um campo com significado diferente.
- Não publique entidade JPA nem payload de endpoint como contrato.
- Não inclua senha, token, CVV ou dados completos de cartão.
- Contratos têm exemplos válidos e inválidos em teste automatizado.
- Mudança incompatível gera nova versão e plano de convivência.
