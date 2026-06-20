# Starter Kit Semaine 18 : Robustesse et performance

## Kit 18.1 — Adaptateur HTTP et simulation Gatling

Gatling cible l’API stable `POST /api/v1/transactions`. L’adaptateur sans Tapir est fourni dans `fil-rouge/src/main/scala/distributed/http/LoadAdapter.scala`. Il transforme chaque requête acceptée en record Kafka et retourne `202` seulement après l’accusé du broker. La S19 refactorise ensuite cette même route avec Tapir et OpenAPI.

```scala
// src/gatling/scala/ClearingLoadSimulation.scala
import io.gatling.core.Predef.*
import io.gatling.http.Predef.*
import scala.concurrent.duration.*

class ClearingLoadSimulation extends Simulation:
  private val httpProtocol = http.baseUrl("http://localhost:8080")

  private val feeder = Iterator.from(1).map { n =>
    Map("txId" -> s"load-$n", "amount" -> ((n % 1000) + 1).toString)
  }

  private val scenarioUnderTest =
    scenario("clearing-load")
      .feed(feeder)
      .exec(
        http("publish-transaction")
          .post("/api/v1/transactions")
          .body(StringBody(
            """{"id":"#{txId}","sender":"AWB","receiver":"CIH","amount":#{amount},"status":"Pending","transactionType":"Transfer"}"""
          ))
          .asJson
          .check(status.is(202))
      )

  setUp(
    scenarioUnderTest.inject(
      rampUsersPerSec(10).to(100).during(60.seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.failedRequests.percent.lt(1.0),
      global.responseTime.percentile(95).lt(500)
    )
```

**TODO stagiaire :** remplacer la création d’un producer par requête par une ressource partagée et scoped, puis vérifier qu’une réponse `202` correspond à un accusé de réception Kafka.

## Kit 18.2 — Toxiproxy local

```yaml
services:
  toxiproxy:
    image: ghcr.io/shopify/toxiproxy
    ports:
      - "8474:8474"
      - "19042:19042"
```

```bash
toxiproxy-cli create cassandra \
  --listen 0.0.0.0:19042 \
  --upstream cassandra:9042

toxiproxy-cli toxic add cassandra \
  --type latency \
  --attribute latency=500
```

L'application de laboratoire se connecte à `toxiproxy:19042`.

## Kit 18.3 — Fiche de profilage reproductible

```text
JVM :
Commit :
Image :
Charge Gatling :
Durée du warm-up :
Durée de capture :
Méthode dominante :
Pourcentage CPU :
Classe dominante en heap :
Chemin vers GC root :
Modification testée :
Résultat après modification :
```

## Kit 18.4 — Matrice JVM

```bash
JAVA_BASE="-Xlog:gc*:file=/tmp/gc.log:time,uptime,level,tags"

# Baseline
java $JAVA_BASE -jar clearing-engine.jar

# G1 borné
java $JAVA_BASE -XX:+UseG1GC -Xms512m -Xmx512m \
  -XX:MaxGCPauseMillis=50 -jar clearing-engine.jar

# Faible heap, uniquement dans le laboratoire
java $JAVA_BASE -Xmx128m -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heap.hprof -jar clearing-engine.jar
```

## Kit 18.5 — Contrat de test HA

```text
Préconditions :
- topic clearing-input : au moins 3 partitions ;
- 3 instances avec le même group.id ;
- IDs métier uniques et persistés ;
- dashboard ouvert sur débit, lag et erreurs.

Mesures :
- affectation avant arrêt ;
- heure d'arrêt ;
- durée du rebalance ;
- lag maximal ;
- IDs relus ;
- IDs manquants ;
- retour au niveau nominal.
```

Commandes de départ :

```bash
docker compose up -d --scale engine=3
docker compose logs -f engine
docker compose ps
```
