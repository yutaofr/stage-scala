---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 8"
footer: "Jour 3 — Appel de Services REST (Java HttpClient)"
---

# Services Externes & HttpClient
## Connecter le moteur de clearing au monde réel

**Durée :** ~2h | **Fil Rouge :** Récupérer les taux de change en direct

---

# 📋 Objectifs du Jour

- Comprendre le fonctionnement d'une requête HTTP.
- Utiliser le `java.net.http.HttpClient` (standard depuis Java 11).
- Gérer les appels synchrones et asynchrones.
- Intégrer des données externes dans le pipeline Scala.

---

# 1. Pourquoi le HttpClient de Java ?

Depuis Java 11, la JVM inclut un client moderne, performant et compatible HTTP/2.

### Avantages
- Pas de dépendances externes (déjà dans le JDK).
- Gestion native des types (String, JSON, Streams).
- Syntaxe fluide (Builder pattern).

---

# 2. Faire sa première requête

```scala
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}

// 1. Créer le client
val client = HttpClient.newHttpClient()

// 2. Construire la requête
val request = HttpRequest.newBuilder()
  .uri(URI.create("https://api.exchangerates.io/latest"))
  .build()

// 3. Envoyer et recevoir
val response = client.send(request, HttpResponse.BodyHandlers.ofString())

println(response.body())
```

---

# 3. Gérer les échecs réseau

Le monde réel est instable. Il faut protéger notre moteur.

```scala
val statusCode = response.statusCode()

if statusCode == 200 then
  // Traitement...
else
  // Gestion d'erreur (Option/Either/Exception)
```

> [!CAUTION]
> Un appel réseau est lent et peut échouer. Ne faites jamais d'appels HTTP à l'intérieur d'une boucle serrée (ex: 1 million de fois). Mettez les résultats en **cache**.

---

# 🏗️ Application : Taux de Change ATH

Le clearing nécessite de convertir toutes les devises étrangères en Dirham (MAD) avant le netting.

```scala
def getMadRate(currency: String): Option[BigDecimal] =
  // Appel HTTP -> Parsing JSON -> Option[BigDecimal]
```

---

# 🧠 Quiz Rapide

1. Quelle classe Java permet de définir l'URL de la requête ? (`URI`).
2. Quelle méthode du client déclenche l'envoi de la requête ? (`send`).
3. Comment éviter de ralentir le moteur lors d'appels réseau ? (Utiliser des caches ou des appels asynchrones).

---

# ⚠️ Compromis du Stage

> [!IMPORTANT]
> **Ce que notre TP simplifie vs la réalité :**
>
> | Aspect | Notre TP | Production réelle |
> |---|---|---|
> | **Source** | Fichier JSON local | API REST réelle (latence, timeout) |
> | **Parsing JSON** | `split(",")` basique | Librairie dédiée (Circe, play-json) |
> | **Erreurs réseau** | Non gérées | `Try` / `Either` pour isolation fonctionnelle |
> | **Mode** | Synchrone bloquant | Asynchrone (`Future`, `CompletableFuture`) |
>
> 👉 **Mois 3** : On introduira `Try` et `Either` pour gérer proprement les erreurs d'appels externes, et on découvrira **Circe** pour le parsing JSON typé.

---

# 📝 Résumé du Jour

- Le `HttpClient` de Java est le pont idéal pour les services externes.
- Les requêtes HTTP sont des opérations "impures" qu'il faut isoler du cœur du moteur.
- Toujours vérifier le `statusCode` avant de traiter le corps de la réponse.
- Tu as maintenant une application "connectée".

**Prochaine étape** : Récupérer les taux réels dans le TP 38 !
