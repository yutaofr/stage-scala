# Starter Kit Semaine 19 : Industrialisation, CI et API

Les artefacts S19 sont fournis dans `fil-rouge/`. Ils utilisent le même build, les mêmes contrats et les mêmes variables d’environnement que les semaines 14 à 18.

## Préflight

```bash
cd fil-rouge
sbt clean test assembly
test -f target/scala-3.3.8/clearing-engine.jar
```

---

## Kit 19.1 — Dockerfile multi-stage

**Fichiers fournis :**

- `fil-rouge/Dockerfile`
- `fil-rouge/.dockerignore`
- `fil-rouge/project/plugins.sbt`

Le build exécute les tests avant `assembly`. L’image runtime :

- utilise Java 21 ;
- copie `clearing-engine.jar` ;
- exécute le processus avec l’utilisateur non privilégié `10001` ;
- expose le port `8080`.

**Zone stagiaire :**

1. Construire l’image.
2. Vérifier l’utilisateur avec `docker inspect`.
3. Comparer la taille des couches.
4. Scanner l’image.
5. Épingler le tag du builder validé par la CI.

---

## Kit 19.2 — Stack Compose exécutable

**Fichiers fournis :**

- `fil-rouge/docker/compose.yml`
- `fil-rouge/docker/init-cassandra.cql`

Le service `engine` consomme réellement :

```yaml
environment:
  KAFKA_BOOTSTRAP_SERVERS: kafka:29092
  CASSANDRA_HOST: cassandra
  CASSANDRA_PORT: 9042
  HTTP_PORT: 8080
```

Kafka annonce `localhost:9092` aux outils locaux et `kafka:29092` aux conteneurs. Cassandra utilise `cassandra:9042` dans le réseau Compose.

Le service `worker` lance `distributed.pipeline.ClearingPipelineApp`. Il complète les projections Cassandra, publie la sortie ou la DLQ, puis valide les offsets.

```bash
cd fil-rouge
docker compose -f docker/compose.yml config
docker compose -f docker/compose.yml up -d --build --wait
docker compose -f docker/compose.yml ps
curl --fail http://localhost:8080/health
```

Si le port local est occupé, définis par exemple `HTTP_HOST_PORT=18083`.

---

## Kit 19.3 — GitHub Actions

**Fichier fourni :** `fil-rouge/.github/workflows/ci.yml`

La CI :

1. compile et teste depuis `fil-rouge/` ;
2. construit l’image uniquement après les tests ;
3. n’accorde aucun droit d’écriture par défaut ;
4. annule les exécutions obsolètes d’une même branche.

**Zone stagiaire :**

- ajouter le cache Coursier ;
- publier uniquement sur un tag `v*` ;
- activer l’authentification GHCR ;
- ajouter un scan de l’image ;
- conserver le build sans publication sur les pull requests.

---

## Kit 19.4 — API HTTP avec HttpServer du JDK

**Fichier fourni :** `fil-rouge/src/main/scala/distributed/http/Api.scala`

Routes stables :

| Méthode | Route | Usage |
|---|---|---|
| GET | `/health` | healthcheck |
| POST | `/api/v1/transactions` | ingestion du contrat `TransactionSubmittedV1` |
| GET | `/api/v1/banks/{bankId}/positions?date=...` | consultation d’une projection |

Chaque handler hérite de `com.sun.net.httpserver.HttpHandler` et traite les requêtes :

```scala
final class IngestionHandler(settings: KafkaSettings) extends HttpHandler:
  def handle(exchange: HttpExchange): Unit =
    if exchange.getRequestMethod != "POST" then
      respond(exchange, 405, ApiError("METHOD_NOT_ALLOWED", "POST only").asJson.noSpaces)
    else
      // lecture corps, décodage JSON et validation...
```

Les erreurs de contrat retournent `400`. Une indisponibilité Kafka retourne `503` après un timeout borné.

**Zone stagiaire :**

1. Injecter un producer Kafka partagé au lieu de le recréer par requête dans les handlers.
2. Brancher les positions sur le repository Cassandra de la semaine 16.
3. Ajouter la lecture de l'historique par date.
4. Tester une erreur `400`, une acceptation `202` et une lecture `200`.

---

## Kit 19.5 — Checklist release candidate

```text
[ ] clean compile
[ ] tests unitaires
[ ] tests d'intégration
[ ] assembly produit
[ ] docker build
[ ] scan image
[ ] docker compose config
[ ] démarrage depuis volumes vides
[ ] healthchecks verts
[ ] scénario nominal
[ ] scénario JSON invalide vers DLQ
[ ] scénario doublon et reprise
[ ] scénario dépendance indisponible
[ ] dashboard provisionné
[ ] Endpoints de l'API accessibles (health, transactions, positions)
[ ] README rejoué par une autre personne
```
