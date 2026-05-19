---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 10"
footer: "Jour 5 — Revue & Clearing Engine v2.1"
---

# Bilan Semaine 10
## Résilience Typée & Railway Architecture

**Durée :** ~2h | **Fil Rouge :** Clearing Engine v2.1 — Zéro Exception

---

# 📋 Objectifs du Jour

- Synthétiser l'usage de `Either` et `Try`.
- Analyser la clarté apportée par le Railway Oriented Programming.
- Comprendre pourquoi un programme sans exceptions est plus sûr.
- Livrer la version 2.1 (Resilient) du moteur.

---

# 1. Rétrospective sur l'Erreur

### Hier (Style Impératif)
- On "croisait les doigts" pour que ça ne plante pas.
- On catchait des exceptions génériques en fin de programme.
- Le flux de données était haché par les `try/catch`.

### Aujourd'hui (Style ROP)
- L'erreur est une **Donnée**.
- Le pipeline gère explicitement le succès et l'échec.
- La signature des fonctions dit la vérité : `(A) => Either[Error, B]`.

---

# 🛡️ La Solidité du Rail

Grâce à `Either` et `Try`, ton moteur est devenu "Railway Oriented" :
- Plus de crashs inattendus.
- Reporting d'erreurs d'une précision chirurgicale.
- Chaque échec est typé et catégorisé dans ton ADT `ClearingError`.

> 💡 Ton code est maintenant capable de tourner 24h/24 sans supervision humaine pour relancer les crashs.

---

# 🏗️ Architecture du Clearing Engine v2.1

```text
Entrée (CSV)
  --> Try(Loading)        --toEither--> Rail
  --> flatMap(Parse)       --> Rail  (ParsingError si échec)
  --> flatMap(Validate)    --> Rail  (ValidationError si échec)
  --> flatMap(Enrich)      --> Rail  (MissingBankName → recoverNonCritical)
  --> map(Netting)         --> Rail
  --> fold(ErrorStats, Report)      --> Sortie
```

> 💡 Grâce à `fold`, le rapport final inclut une synthèse analytique des erreurs par catégorie (Parsing, Validation, Technique, Business).

---

# 🚀 Vers la Semaine 11

Le mois 3 continue avec de l'abstraction pure :
1. **Type Classes** : Séparer les données des comportements.
2. **Context Parameters** : Injecter de la logique implicitement.
3. **Extension Methods** : Ajouter des méthodes à des classes sans les modifier.
4. **Opaque Types** : Sécurité de typage avec 0 coût mémoire.

---

# 🔮 Les Limites Actuelles (Pont vers le Mois 4)

Tu l'as bien identifié dans ta rétro, Thomas. Voici les problèmes concrets que le Mois 4 va résoudre :

| Limite actuelle | Solution Mois 4 |
|---|---|
| `Files.readAllLines()` charge tout en RAM | **ZIO Streams** : traitement ligne par ligne |
| `fetchLatestRates()` est bloquant | **ZIO Fibers** : appels asynchrones non-bloquants |
| `println()` pour les rapports | **Kafka Producer** : publication d'événements |
| Pas de persistance | **Cassandra/Postgres** : stockage durable |
| Monitoring par console | **Prometheus/Grafana** : métriques temps réel |

---

# 🧠 Quiz de Fin de Semaine

1. Quelle méthode de `Either` permet de transformer les deux rails en une seule valeur ? (`fold` — attention, `bimap` n'existe pas en Scala standard, c'est un ajout de Cats/Scalaz).
2. Pourquoi préférer `for-yield` à une suite d'appels `flatMap` manuels ? (Lisibilité).
3. Quel type utiliser pour emballer un appel à une bibliothèque Java instable ? (`Try`).

---

# 📝 Conclusion

Bravo ! Tu as dompté l'erreur informatique. Ton code n'est plus seulement fonctionnel, il est **Résilient**. C'est une compétence clé pour travailler sur des systèmes de flux financiers critiques comme ceux d'ATH.

**Dernière étape** : Finaliser la v2.1 dans le TP 50 !
