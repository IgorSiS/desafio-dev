# metrics-flex

> **Instrumentação declarativa de métricas de domínio para aplicações Spring Boot**

[![Java 17](https://img.shields.io/badge/Java-17-blue)](https://adoptium.net/)
[![Spring Boot 3.x](https://img.shields.io/badge/Spring%20Boot-3.x-green)](https://spring.io/projects/spring-boot)
[![Micrometer](https://img.shields.io/badge/Micrometer-compatível-informational)](https://micrometer.io/)
[![Versão](https://img.shields.io/badge/versão-0.1.0--SNAPSHOT-yellow)](./build.gradle)

---

## Índice

1. [Visão Geral](#1-visão-geral)
2. [Módulos](#2-módulos)
3. [Instalação](#3-instalação)
4. [Uso da Anotação `@MonitorMetric`](#4-uso-da-anotação-monitormetric)
5. [Controle de Cardinalidade (CardinalityGuard)](#5-controle-de-cardinalidade-cardinalityguard)
6. [Configuração (Properties)](#6-configuração-properties)
7. [Extensão via SPI](#7-extensão-via-spi)
8. [Boas Práticas](#8-boas-práticas)
9. [Build e Testes](#9-build-e-testes)
10. [Roadmap](#10-roadmap)

---

## 1. Visão Geral

### O problema que esta biblioteca resolve

Aplicações modernas já contam com stacks robustas de observabilidade de infraestrutura — Micrometer, Prometheus, OpenTelemetry — que cuidam da *mecânica* de exportar métricas: formatos, endpoints, scraping, retenção.

O que falta é uma camada de **instrumentação de domínio**: uma forma padronizada e declarativa de responder às perguntas:

- *O que* medir (nomes e semântica das métricas)?
- *Quais dimensões* (tags/labels) cada métrica deve ter?
- *Como* declarar isso sem boilerplate espalhado pelo código?

```
┌──────────────────────────────────────────────────┐
│              Sua Aplicação de Negócio             │
│   @MonitorMetric  ←── métricas de domínio        │
└───────────────────────┬──────────────────────────┘
                        │ MetricEvent
┌───────────────────────▼──────────────────────────┐
│              metrics-flex (esta lib)              │
│   Aspecto AOP → TagResolver → MetricPublisher     │
└───────────────────────┬──────────────────────────┘
                        │ registra via
┌───────────────────────▼──────────────────────────┐
│         Infraestrutura de Observabilidade         │
│         Micrometer / Prometheus / OTel            │
└──────────────────────────────────────────────────┘
```

### Fronteira de responsabilidade

| Camada                | Responsabilidade                                   |
|-----------------------|----------------------------------------------------|
| **Infra (Micrometer)**| *Como* exportar: formato, endpoint, configuração   |
| **metrics-flex**      | *O que* medir e *como declarar* via anotação       |

### Benefícios

- **Padronização**: nomes, tags e semântica definidos uma única vez na anotação
- **Zero boilerplate**: sem `registry.counter(...)` espalhado no código de negócio
- **Controle de cardinalidade**: proteção declarativa contra explosão de labels
- **Testabilidade**: troque `MicrometerMetricPublisher` por um publisher de stub nos testes
- **Extensibilidade**: SPIs para publisher e resolver de tags completamente substituíveis

---

## 2. Módulos

O projeto é organizado em três módulos Gradle com separação clara de responsabilidades:

```
lib-metrics-flex/
├── metrics-flex-core/                    ← contratos públicos (sem dependências de runtime)
├── metrics-flex-spring-boot-autoconfigure/ ← implementação e auto-configuração Spring Boot
└── metrics-flex-spring-boot-starter/    ← dependência de conveniência para consumidores
```

### `metrics-flex-core`

Pacote base: `io.poc.observability.metricsflex.core`

Contém apenas os contratos públicos da biblioteca — sem dependência de framework:

| Tipo | Classe / Interface | Responsabilidade |
|------|--------------------|-----------------|
| Anotação | `MonitorMetric` | Declara a métrica sobre métodos e tipos |
| Enum | `MetricKind` | `COUNTER`, `TIMER`, `GAUGE` |
| Record | `MetricEvent` | Representação imutável de um evento de métrica |
| SPI | `MetricPublisher` | Contrato para publicação da métrica |
| SPI | `MetricTagResolver` | Contrato para resolução de tags |

### `metrics-flex-spring-boot-autoconfigure`

Pacote base: `io.poc.observability.metricsflex.autoconfigure`

Implementação completa com integração Spring Boot:

| Classe | Responsabilidade |
|--------|-----------------|
| `MetricsFlexAutoConfiguration` | Configura os beans automaticamente via `@AutoConfiguration` |
| `MetricsFlexProperties` | Lê propriedades do `application.yml` com prefixo `metrics.flex` |
| `MonitorMetricAspect` | Aspecto AOP que intercepta métodos anotados |
| `MicrometerMetricPublisher` | Publica métricas via `MeterRegistry` do Micrometer |
| `SpelMetricTagResolver` | Resolve expressões de tags declaradas na anotação |

### `metrics-flex-spring-boot-starter`

Módulo vazio que agrega as dependências necessárias. Consumidores da lib adicionam **apenas este módulo** no seu `build.gradle` ou `pom.xml`.

---

## 3. Instalação

### Gradle (recomendado)

```gradle
dependencies {
    implementation 'io.poc.engineering.libraries:metrics-flex-spring-boot-starter:0.1.0-SNAPSHOT'
}
```

### Maven

```xml
<dependency>
    <groupId>io.poc.engineering.libraries</groupId>
    <artifactId>metrics-flex-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Como funciona a auto-configuração

A classe `MetricsFlexAutoConfiguration` é registrada no arquivo:

```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

O Spring Boot a carrega automaticamente. A configuração só é ativada quando:

1. `@ConditionalOnClass(MeterRegistry.class)` — o Micrometer está no classpath (ou seja, alguma dependência de métricas está presente, como `spring-boot-starter-actuator`)
2. `@ConditionalOnProperty(prefix = "metrics.flex", name = "enabled", havingValue = "true", matchIfMissing = true)` — a propriedade `metrics.flex.enabled` não está definida como `false`

Isso significa que a lib é **opt-out**: funciona por padrão, e pode ser desligada explicitamente com `metrics.flex.enabled=false`.

---

## 4. Uso da Anotação `@MonitorMetric`

### Atributos da anotação

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitorMetric {

    String name();                            // (obrigatório) nome da métrica

    MetricKind kind() default MetricKind.COUNTER; // tipo: COUNTER, TIMER, GAUGE

    String description() default "";         // descrição da métrica (aparece no HELP)

    String[] tags() default {};              // tags no formato "chave=valor"

    String value() default "1";             // valor a incrementar/registrar
}
```

**Formato das tags:** cada elemento do array `tags` segue o padrão `"chave=valor"`, onde o valor é uma string literal. O separador é o primeiro `=` encontrado.

**Tag `status` automática:** o aspecto `MonitorMetricAspect` sempre adiciona a tag `status` com o valor `"success"` se o método concluiu normalmente ou `"error"` se lançou uma exceção — sem necessidade de declarar essa tag manualmente.

**Sufixo `_total`:** o Micrometer adiciona automaticamente o sufixo `_total` a contadores quando exposto no formato Prometheus, seguindo a convenção OpenMetrics.

---

### Exemplo 1 — Contador simples com `name` apenas

O caso mais básico: apenas o nome da métrica é obrigatório.

```java
import io.poc.observability.metricsflex.core.annotation.MonitorMetric;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {

    @MonitorMetric(name = "pedidos_processados")
    public void processarPedido(String pedidoId) {
        // lógica de processamento
    }
}
```

**Explicação dos atributos:**

| Atributo | Valor | Efeito |
|----------|-------|--------|
| `name` | `"pedidos_processados"` | Nome base da métrica |
| `kind` | `COUNTER` (padrão) | Tipo contador |
| `value` | `"1"` (padrão) | Incrementa 1 a cada chamada |
| `tags` | `{}` (padrão) | Sem tags customizadas |

A única tag presente é `status`, adicionada automaticamente pelo aspecto.

**Resultado no monitoramento (`/actuator/prometheus`):**

```
# HELP pedidos_processados_total 
# TYPE pedidos_processados_total counter
pedidos_processados_total{status="success"} 42.0
```

Se o método lançar uma exceção:

```
pedidos_processados_total{status="error"} 3.0
```

**Query PromQL — taxa de processamento:**

```promql
rate(pedidos_processados_total[5m])
```

**Painel:** Em Grafana, essa query mostra a taxa de pedidos processados por segundo nos últimos 5 minutos, com separação por status de execução.

---

### Exemplo 2 — Consumer Kafka com tag literal e tag de argumento

Instrumentação de um consumer Kafka com tags fixas (literais) e tags derivadas do argumento recebido.

```java
import io.poc.observability.metricsflex.core.annotation.MonitorMetric;
import io.poc.observability.metricsflex.core.model.MetricKind;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PagamentoConsumer {

    @KafkaListener(topics = "payments")
    @MonitorMetric(
        name = "kafka_messages",
        kind = MetricKind.COUNTER,
        description = "Mensagens processadas pelo consumer de pagamentos",
        tags = {
            "topic=payments",
            "eventType=AUTHORIZED"
        }
    )
    public void consumir(PagamentoEvent evento) {
        // processamento da mensagem
    }
}
```

**Explicação dos atributos:**

| Atributo | Valor | Efeito |
|----------|-------|--------|
| `name` | `"kafka_messages"` | Nome base (ficará `kafka_messages_total` no Prometheus) |
| `kind` | `COUNTER` | Tipo contador |
| `description` | `"Mensagens processadas..."` | Aparece na linha `# HELP` do Prometheus |
| `tags[0]` | `"topic=payments"` | Tag fixa com valor `payments` |
| `tags[1]` | `"eventType=AUTHORIZED"` | Tag fixa com valor `AUTHORIZED` |

As tags são declaradas como pares `chave=valor`. O valor à direita do `=` é tratado como string literal.

**Resultado no monitoramento — caso sucesso:**

```
# HELP kafka_messages_total Mensagens processadas pelo consumer de pagamentos
# TYPE kafka_messages_total counter
kafka_messages_total{eventType="AUTHORIZED",status="success",topic="payments"} 128.0
```

**Resultado no monitoramento — caso erro** (o método lançou uma exceção):

```
# HELP kafka_messages_total Mensagens processadas pelo consumer de pagamentos
# TYPE kafka_messages_total counter
kafka_messages_total{eventType="AUTHORIZED",status="error",topic="payments"} 5.0
```

O aspecto captura qualquer `Throwable` lançado pelo método, registra a métrica com `status=error`, e relança a exceção normalmente.

**Query PromQL — taxa de erros:**

```promql
sum by (topic) (
  rate(kafka_messages_total{status="error"}[5m])
)
```

**Painel:** No Grafana, é possível criar um alerta quando a taxa de `status=error` ultrapassar um limiar, segmentado por `topic`.

---

### Exemplo 3 — Service de negócio com múltiplas tags e `description`

Instrumentação de um serviço que processa transações, com tags de segmentação para análise de negócio.

```java
import io.poc.observability.metricsflex.core.annotation.MonitorMetric;
import io.poc.observability.metricsflex.core.model.MetricKind;
import org.springframework.stereotype.Service;

@Service
public class TransacaoService {

    @MonitorMetric(
        name = "transacoes_autorizadas",
        kind = MetricKind.COUNTER,
        description = "Total de transações autorizadas por tipo de cliente e canal",
        tags = {
            "customerType=PREMIUM",
            "channel=MOBILE"
        },
        value = "1"
    )
    public AutorizacaoResponse autorizar(AutorizacaoRequest request) {
        // lógica de autorização
        return new AutorizacaoResponse("APPROVED");
    }
}
```

**Explicação dos atributos:**

| Atributo | Valor | Efeito |
|----------|-------|--------|
| `name` | `"transacoes_autorizadas"` | Nome da métrica |
| `kind` | `COUNTER` | Tipo contador |
| `description` | `"Total de transações..."` | Documentação da métrica no Prometheus |
| `tags[0]` | `"customerType=PREMIUM"` | Segmenta por tipo de cliente |
| `tags[1]` | `"channel=MOBILE"` | Segmenta por canal de acesso |
| `value` | `"1"` | Incremento unitário por chamada |

**Resultado no monitoramento:**

```
# HELP transacoes_autorizadas_total Total de transações autorizadas por tipo de cliente e canal
# TYPE transacoes_autorizadas_total counter
transacoes_autorizadas_total{channel="MOBILE",customerType="PREMIUM",status="success"} 312.0
transacoes_autorizadas_total{channel="MOBILE",customerType="PREMIUM",status="error"} 8.0
```

**Query PromQL — taxa de autorização com sucesso por canal:**

```promql
sum by (channel, customerType) (
  rate(transacoes_autorizadas_total{status="success"}[5m])
)
```

**Query PromQL — percentual de erros por tipo de cliente:**

```promql
sum by (customerType) (rate(transacoes_autorizadas_total{status="error"}[5m]))
/
sum by (customerType) (rate(transacoes_autorizadas_total[5m]))
```

---

### Exemplo 4 — Handler HTTP com tag do retorno

Instrumentação de um endpoint REST, com tag extraída do objeto retornado pelo método.

```java
import io.poc.observability.metricsflex.core.annotation.MonitorMetric;
import io.poc.observability.metricsflex.core.model.MetricKind;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @PostMapping
    @MonitorMetric(
        name = "http_pedidos_criados",
        kind = MetricKind.COUNTER,
        description = "Pedidos criados via API REST",
        tags = {
            "endpoint=POST_pedidos",
            "versao=v1"
        }
    )
    public ResponseEntity<PedidoCriadoResponse> criarPedido(@RequestBody CriarPedidoRequest request) {
        // lógica de criação
        return ResponseEntity.ok(new PedidoCriadoResponse("PED-001", "CREATED"));
    }
}
```

**Explicação dos atributos:**

| Atributo | Valor | Efeito |
|----------|-------|--------|
| `name` | `"http_pedidos_criados"` | Nome da métrica |
| `tags[0]` | `"endpoint=POST_pedidos"` | Identifica o endpoint (formato HTTP_VERB_RECURSO) |
| `tags[1]` | `"versao=v1"` | Versão da API |

**Resultado no monitoramento:**

```
# HELP http_pedidos_criados_total Pedidos criados via API REST
# TYPE http_pedidos_criados_total counter
http_pedidos_criados_total{endpoint="POST_pedidos",status="success",versao="v1"} 89.0
http_pedidos_criados_total{endpoint="POST_pedidos",status="error",versao="v1"} 2.0
```

**Query PromQL — disponibilidade do endpoint:**

```promql
1 - (
  sum(rate(http_pedidos_criados_total{status="error"}[5m]))
  /
  sum(rate(http_pedidos_criados_total[5m]))
)
```

**Painel:** No Dynatrace, a métrica pode ser importada com o prefixo `dyna_` para criar SLOs de disponibilidade por endpoint.

---

### Exemplo 5 — Anotação em nível de classe (`@Target(TYPE)`)

A anotação `@MonitorMetric` suporta `ElementType.TYPE`, o que permite anotar uma classe inteira. Nesse caso, todos os métodos públicos da classe são instrumentados com a mesma configuração base.

```java
import io.poc.observability.metricsflex.core.annotation.MonitorMetric;
import io.poc.observability.metricsflex.core.model.MetricKind;
import org.springframework.stereotype.Service;

@Service
@MonitorMetric(
    name = "auditoria_operacoes",
    kind = MetricKind.COUNTER,
    description = "Operações auditadas do serviço de contas",
    tags = {
        "servico=conta-service",
        "ambiente=producao"
    }
)
public class ContaService {

    public void criarConta(CriarContaRequest request) {
        // todos os métodos desta classe são monitorados
    }

    public ContaResponse buscarConta(String id) {
        return new ContaResponse(id);
    }

    public void encerrarConta(String id) {
        // também monitorado
    }
}
```

**Explicação:**

A anotação em nível de classe aplica a mesma configuração a cada método público interceptado pelo aspecto. É útil para serviços de auditoria onde toda operação deve ser contabilizada uniformemente.

> ⚠️ **Atenção:** ao anotar uma classe, o mesmo `name` é usado para todos os métodos, o que pode causar sobreposição de métricas se os métodos representam operações distintas. Prefira anotar cada método individualmente quando as operações têm semânticas diferentes.

**Resultado no monitoramento:**

```
# HELP auditoria_operacoes_total Operações auditadas do serviço de contas
# TYPE auditoria_operacoes_total counter
auditoria_operacoes_total{ambiente="producao",servico="conta-service",status="success"} 450.0
auditoria_operacoes_total{ambiente="producao",servico="conta-service",status="error"} 12.0
```

---

### Exemplo 6 — Uso de `value` customizado

O atributo `value` define o número incrementado/registrado a cada chamada. É útil para contadores de quantidade (ex.: número de itens processados em batch).

```java
import io.poc.observability.metricsflex.core.annotation.MonitorMetric;
import io.poc.observability.metricsflex.core.model.MetricKind;
import org.springframework.stereotype.Service;

@Service
public class BatchService {

    @MonitorMetric(
        name = "batch_itens_processados",
        kind = MetricKind.COUNTER,
        description = "Itens processados em lote",
        tags = {
            "job=importacao-diaria",
            "tipo=produto"
        },
        value = "100"
    )
    public void processarLote() {
        // processa exatamente 100 itens por execução
    }
}
```

**Explicação:**

| Atributo | Valor | Efeito |
|----------|-------|--------|
| `value` | `"100"` | Incrementa o contador em 100 a cada execução do método |

O `value` é parseado como `double` pelo aspecto: `Double.parseDouble(monitorMetric.value())`.

**Resultado no monitoramento:**

```
# HELP batch_itens_processados_total Itens processados em lote
# TYPE batch_itens_processados_total counter
batch_itens_processados_total{job="importacao-diaria",status="success",tipo="produto"} 5000.0
```

**Query PromQL — total de itens processados por job:**

```promql
sum by (job) (batch_itens_processados_total{status="success"})
```

---

### Exemplo 7 — Desabilitando a lib para testes ou ambientes específicos

```java
// application-test.yml
// metrics.flex.enabled: false
```

Quando `enabled=false`, nenhum aspecto é ativado e nenhuma métrica é publicada.

```yaml
# application-test.yml
metrics:
  flex:
    enabled: false
```

---

## 5. Controle de Cardinalidade (CardinalityGuard)

### Por que cardinalidade alta é perigosa?

Em sistemas de métricas como Prometheus, cada combinação única de tags (labels) cria uma série temporal separada no banco de dados. Isso é chamado de **cardinalidade**.

**Exemplo do problema:**

```java
// ❌ ERRADO — nunca coloque IDs em tags!
@MonitorMetric(
    name = "pedidos_processados",
    tags = {
        "pedidoId=ABC-001",   // cada pedido = nova série temporal!
        "clienteId=12345"     // cardinalidade explode com o volume
    }
)
public void processar(Pedido pedido) { ... }
```

Com 1 milhão de pedidos únicos, isso criaria 1 milhão de séries temporais, sobrecarregando o Prometheus ou qualquer backend de métricas.

### Comportamento esperado do CardinalityGuard (v2)

> 🚧 **Planejado para v2:** O `CardinalityGuard` está no roadmap da biblioteca. A seção abaixo descreve o comportamento projetado.

O `CardinalityGuard` atuará como uma camada de proteção entre o `MetricTagResolver` e o `MetricPublisher`, com as seguintes responsabilidades:

| Comportamento | Descrição |
|---------------|-----------|
| **Normalização** | Converte chaves de tags para `snake_case` e `lowercase` automaticamente |
| **Denylist** | Remove tags cujas chaves estejam na lista negra (`denied-tag-keys`) |
| **Limite máximo** | Rejeita tags além do limite configurado (`max-tags`), emitindo `WARN` no log |

**Exemplo projetado — tag proibida ausente no resultado:**

```java
// Configuração:
// metrics.flex.denied-tag-keys: orderId, customerId

@MonitorMetric(
    name = "pedidos_processados",
    tags = {
        "status_processamento=APROVADO",
        "orderId=ABC-001"           // ← tag proibida pela denylist
    }
)
public void processar(Pedido pedido) { ... }
```

**Resultado no monitoramento** — a tag proibida é silenciosamente removida:

```
# HELP pedidos_processados_total 
# TYPE pedidos_processados_total counter
pedidos_processados_total{status="success",status_processamento="APROVADO"} 1.0
```

Log emitido:

```
WARN  CardinalityGuard - Tag 'orderId' removida: chave presente na denylist [orderId, customerId]
```

**Exemplo projetado — estouro de `max-tags`:**

```java
// metrics.flex.max-tags: 3

@MonitorMetric(
    name = "evento_complexo",
    tags = {
        "origem=kafka",
        "tipo=pagamento",
        "regiao=sudeste",
        "canal=mobile",    // ← tags além do limite são ignoradas
        "versao=v2"        // ← idem
    }
)
public void processar(Evento evento) { ... }
```

**Resultado:**

```
# HELP evento_complexo_total 
# TYPE evento_complexo_total counter
evento_complexo_total{canal="mobile",origem="kafka",status="success",tipo="pagamento"} 1.0
```

Log emitido:

```
WARN  CardinalityGuard - Tags truncadas: limite de 3 atingido. Tags ignoradas: [regiao, versao]
```

---

## 6. Configuração (Properties)

### Properties disponíveis (v0.1.0-SNAPSHOT)

Prefixo: `metrics.flex`

| Property | Tipo | Padrão | Descrição |
|----------|------|--------|-----------|
| `metrics.flex.enabled` | `boolean` | `true` | Habilita ou desabilita toda a instrumentação declarativa da lib |

### Properties planejadas (v2)

| Property | Tipo | Padrão previsto | Descrição |
|----------|------|-----------------|-----------|
| `metrics.flex.max-tags` | `int` | `10` | Número máximo de tags por métrica (CardinalityGuard) |
| `metrics.flex.denied-tag-keys` | `List<String>` | `[]` | Chaves de tags que serão sempre removidas (ex.: `orderId`, `userId`) |

### Exemplos de configuração

**`application.yml`:**

```yaml
metrics:
  flex:
    enabled: true
```

**`application.properties`:**

```properties
metrics.flex.enabled=true
```

**Desabilitando a lib em testes:**

```yaml
# src/test/resources/application.yml
metrics:
  flex:
    enabled: false
```

**Desabilitando em ambiente específico via variável de ambiente:**

```yaml
metrics:
  flex:
    enabled: ${METRICS_FLEX_ENABLED:true}
```

---

## 7. Extensão via SPI

A biblioteca fornece duas interfaces de extensão (SPIs) que permitem substituir comportamentos padrão sem modificar a lib.

### 7.1 Substituindo o `MetricPublisher`

O contrato `MetricPublisher` define como um `MetricEvent` é publicado:

```java
package io.poc.observability.metricsflex.core.spi;

import io.poc.observability.metricsflex.core.model.MetricEvent;

public interface MetricPublisher {
    void publish(MetricEvent event);
}
```

O `MetricEvent` é um record imutável com todos os dados da métrica:

```java
package io.poc.observability.metricsflex.core.model;

import java.util.Map;

public record MetricEvent(
        String name,
        MetricKind kind,
        double value,
        Map<String, String> tags,
        String description
) {}
```

**Exemplo — publisher customizado com auditoria em log:**

```java
import io.poc.observability.metricsflex.core.model.MetricEvent;
import io.poc.observability.metricsflex.core.spi.MetricPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MetricPublisherConfig {

    @Bean
    @Primary  // substitui o MicrometerMetricPublisher padrão
    public MetricPublisher auditingMetricPublisher(
            io.micrometer.core.instrument.MeterRegistry registry
    ) {
        return new AuditingMetricPublisher(registry);
    }
}

public class AuditingMetricPublisher implements MetricPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuditingMetricPublisher.class);
    private final io.micrometer.core.instrument.MeterRegistry registry;

    public AuditingMetricPublisher(io.micrometer.core.instrument.MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void publish(MetricEvent event) {
        // 1. Auditoria estruturada em log
        log.info("METRICA_AUDITORIA nome={} kind={} value={} tags={}",
                event.name(), event.kind(), event.value(), event.tags());

        // 2. Publicação normal via Micrometer
        List<io.micrometer.core.instrument.Tag> tags = event.tags().entrySet().stream()
                .map(e -> io.micrometer.core.instrument.Tag.of(e.getKey(), e.getValue()))
                .toList();

        if (event.kind() == io.poc.observability.metricsflex.core.model.MetricKind.COUNTER) {
            io.micrometer.core.instrument.Counter.builder(event.name())
                    .description(event.description())
                    .tags(tags)
                    .register(registry)
                    .increment(event.value());
        }
    }
}
```

**Log gerado a cada chamada anotada:**

```
INFO  AuditingMetricPublisher - METRICA_AUDITORIA nome=kafka_messages kind=COUNTER value=1.0 tags={topic=payments, status=success}
```

> **Nota:** A auto-configuração registra o `MicrometerMetricPublisher` com `@Bean` simples (sem `@ConditionalOnMissingBean` na v0.1.0). Para substituí-lo, use `@Primary` no seu bean customizado.

### 7.2 Substituindo o `MetricTagResolver`

O contrato `MetricTagResolver` define como as expressões de tags declaradas na anotação são resolvidas em pares `chave=valor`:

```java
package io.poc.observability.metricsflex.core.spi;

import org.aspectj.lang.ProceedingJoinPoint;
import java.util.Map;

public interface MetricTagResolver {
    Map<String, String> resolve(
        ProceedingJoinPoint joinPoint,
        String[] tagExpressions,
        Object result
    );
}
```

**Parâmetros disponíveis no `resolve`:**

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `joinPoint` | `ProceedingJoinPoint` | Contexto AOP com acesso a `getArgs()`, `getTarget()`, `getSignature()` |
| `tagExpressions` | `String[]` | Array de strings declaradas em `tags = {}` da anotação |
| `result` | `Object` | Valor retornado pelo método (`null` se `void` ou se houve exceção) |

**Exemplo — resolver customizado com acesso ao contexto de segurança:**

```java
import io.poc.observability.metricsflex.core.spi.MetricTagResolver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TagResolverConfig {

    @Bean
    @Primary
    public MetricTagResolver securityAwareTagResolver() {
        return new SecurityAwareTagResolver();
    }
}

public class SecurityAwareTagResolver implements MetricTagResolver {

    @Override
    public Map<String, String> resolve(
            ProceedingJoinPoint joinPoint,
            String[] tagExpressions,
            Object result
    ) {
        Map<String, String> tags = new HashMap<>();

        // resolve tags literais declaradas na anotação
        for (String expr : tagExpressions) {
            String[] parts = expr.split("=", 2);
            if (parts.length == 2) {
                tags.put(parts[0].trim(), parts[1].trim());
            }
        }

        // enriquece com contexto de segurança automaticamente
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            tags.put("perfil", auth.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse("desconhecido"));
        }

        return tags;
    }
}
```

---

## 8. Boas Práticas

### ✅ Nomes de métricas

- Use `snake_case` para o `name` da métrica (ex.: `pedidos_autorizados`, `kafka_messages`)
- O sufixo `_total` será adicionado automaticamente pelo Micrometer para contadores; **não** adicione manualmente
- Prefira nomes que descrevam **o que aconteceu** (verbo no passado) ou **o que está sendo contado**
- Evite nomes genéricos demais (`operacao_executada`) ou específicos demais (`payment_service_authorize_v2_attempt_3`)

```java
// ✅ Bom
@MonitorMetric(name = "autorizacoes_cartao")

// ❌ Evite — muito genérico
@MonitorMetric(name = "operacao")

// ❌ Evite — sufixo _total duplicado
@MonitorMetric(name = "pedidos_total")
```

### ✅ Tags com baixa cardinalidade

Nunca coloque valores que mudam por registro em tags:

```java
// ❌ ERRADO — cardinalidade explode com o volume de dados
@MonitorMetric(
    name = "pedidos",
    tags = {
        "pedidoId=PED-12345",   // ← ID único = milhões de séries temporais
        "emailCliente=user@exemplo.com"  // ← alto volume
    }
)

// ✅ CORRETO — use categorias de baixa cardinalidade
@MonitorMetric(
    name = "pedidos",
    tags = {
        "canal=MOBILE",          // ← poucos valores possíveis
        "tipoCliente=PREMIUM"    // ← enumerável
    }
)
```

Exemplos de tags **aceitáveis** (baixa cardinalidade):
- `status`, `canal`, `regiao`, `tipoCliente`, `ambiente`, `versao`

Exemplos de tags **proibidas** (alta cardinalidade):
- `orderId`, `customerId`, `userId`, `sessionId`, `email`, `cpf`, `traceId`

### ✅ Testando com `SimpleMeterRegistry`

Em testes unitários, substitua o `MeterRegistry` real por `SimpleMeterRegistry` para verificar se as métricas estão sendo publicadas corretamente:

```java
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.poc.observability.metricsflex.autoconfigure.publisher.MicrometerMetricPublisher;
import io.poc.observability.metricsflex.core.model.MetricEvent;
import io.poc.observability.metricsflex.core.model.MetricKind;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MicrometerMetricPublisherTest {

    @Test
    void devePublicarContadorComTagsCorretas() {
        var registry = new SimpleMeterRegistry();
        var publisher = new MicrometerMetricPublisher(registry);

        publisher.publish(new MetricEvent(
            "kafka_messages",
            MetricKind.COUNTER,
            1.0,
            Map.of("topic", "payments", "status", "success"),
            "Mensagens Kafka"
        ));

        var counter = registry.find("kafka_messages")
            .tags("topic", "payments", "status", "success")
            .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }
}
```

### ✅ Outras recomendações

- Prefira `MetricKind.COUNTER` para eventos discretos (mensagens, transações, erros)
- Use `description` sempre que a métrica não for autoexplicativa — ela aparece no `# HELP` do Prometheus
- Mantenha os nomes de métricas **estáveis** entre versões; renomear uma métrica quebra dashboards e alertas existentes
- Documente as métricas expostas pela sua aplicação em um `METRICS.md` no repositório

---

## 9. Build e Testes

O projeto utiliza Gradle com wrapper. Todos os comandos devem ser executados a partir da raiz do `lib-metrics-flex/`.

### Compilar todos os módulos

```bash
./gradlew build
```

### Executar testes de um módulo específico

```bash
# Testes do módulo core
./gradlew :metrics-flex-core:test

# Testes do módulo de auto-configuração
./gradlew :metrics-flex-spring-boot-autoconfigure:test
```

### Executar todos os testes

```bash
./gradlew test
```

### Publicar localmente (para uso em outros projetos do monorepo)

```bash
./gradlew publishToMavenLocal
```

### Estrutura dos módulos no `settings.gradle`

```gradle
rootProject.name = 'metrics-flex'
include 'metrics-flex-core'
include 'metrics-flex-spring-boot-autoconfigure'
include 'metrics-flex-spring-boot-starter'
```

### Informações de versão (`build.gradle` raiz)

```gradle
allprojects {
    group = 'io.poc.engineering.libraries'
    version = '0.1.0-SNAPSHOT'
}
```

---

## 10. Roadmap

### v2 — Resolução dinâmica e tipos de métrica completos

**SpEL completo nos resolvers de tags:**

Atualmente o `SpelMetricTagResolver` trata os valores das tags como literais. Na v2, o resolver avaliará expressões [Spring Expression Language (SpEL)](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions), permitindo acesso aos argumentos do método, ao objeto alvo e ao resultado:

```java
// sintaxe pretendida para v2
@MonitorMetric(
    name = "kafka_messages",
    tags = {
        "topic='payments'",              // literal com aspas simples
        "eventType=#{#p0.type}",         // #p0 = primeiro argumento
        "resultado=#{#result.status}"    // #result = retorno do método
    }
)
public ProcessResult consumir(PaymentEvent event) { ... }
```

**`MetricExtractor` por fonte:**

Interface que permite extratores específicos por tipo de entrada:

```java
// interface pretendida
public interface MetricExtractor<T> {
    boolean supports(T source);
    MetricEvent extract(T source);
}
```

Implementações por contexto: `KafkaMetricExtractor`, `HttpMetricExtractor`, `ScheduledMetricExtractor`.

**TIMER e GAUGE com duração real:**

```java
@MonitorMetric(name = "tempo_autorizacao", kind = MetricKind.TIMER)
public AutorizacaoResponse autorizar(AutorizacaoRequest req) { ... }
// → registra a duração real da execução do método
```

**CardinalityGuard:**

Proteção declarativa contra explosão de séries temporais, com normalização, denylist e limite máximo de tags por métrica.

---

### v3 — Composição e correlação

**Publishers compostos:**

```java
// combinar múltiplos publishers
@Bean
public MetricPublisher compositePublisher(
    MicrometerMetricPublisher micrometer,
    AuditLogMetricPublisher auditLog
) {
    return new CompositeMetricPublisher(micrometer, auditLog);
}
```

**Correlação com traces e logs:**

Enriquecimento automático das métricas com `traceId` e `spanId` do contexto atual (OpenTelemetry / Sleuth), sem necessidade de declarar nas tags.

**Métricas de negócio com valor dinâmico:**

```java
// valor extraído do resultado do método via SpEL
@MonitorMetric(
    name = "valor_transacao",
    kind = MetricKind.GAUGE,
    value = "#{#result.amount}"   // valor financeiro da transação
)
public Transacao processar(TransacaoRequest request) { ... }
```

---

## Licença

Este projeto faz parte do ecossistema de bibliotecas de engenharia internas (`io.poc.engineering.libraries`).

---

*Documentação gerada para `metrics-flex` v0.1.0-SNAPSHOT · Última atualização: 2026-07*
