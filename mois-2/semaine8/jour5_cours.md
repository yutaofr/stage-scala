---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 8"
footer: "Jour 5 — Demo Mois 2 (Clearing Engine v1.3)"
---

# Bilan du Mois 2
## Modélisation, Algorithmes & Écosystème

**Durée :** ~2h | **Fil Rouge :** Clearing Engine v1.3 — Le Prototype "Connecté"

---

# 📋 Objectifs du Jour

- Récapituler les avancées du mois (ADTs, Netting Multilatéral, Interop).
- Évaluer l'intégration de la logique Scala dans un environnement Java/Spring.
- Démontrer le fonctionnement en condition réelle (HTTP, Sécurité).
- Découvrir le programme du Mois 3 : Concepts Avancés de FP.

---

# 1. Rétrospective du Mois 2

### Semaine 5 & 6 : Modélisation
- Fin des Tuples, bienvenue aux **ADTs**.
- Validation robuste et exhaustive.

### Semaine 7 : Business Logic
- Algorithme de **Netting Multilatéral**.
- Performance sur 1 million de transactions.

### Semaine 8 : Écosystème
- Appel d'APIs externes via **HttpClient**.
- Anonymisation **SHA-256**.
- Structure compatible **Spring Boot**.

---

# 🛡️ État des Lieux du Moteur (v1.3)

Ton application est maintenant un véritable "Agent de Compensation" :
- Il consomme des données (CSV).
- Il se sécurise (Hashing).
- Il s'informe (Taux de change HTTP).
- Il calcule (Netting Multilatéral).
- Il rapporte (Business Reporting).

---

# 🚀 Vers le Mois 3 : Concepts Avancés

Le mois prochain, nous allons **combler les lacunes** identifiées cette semaine :

| Frustration actuelle | Solution Mois 3 |
|---|---|
| JSON parsé "à la main" (`split`) | **Circe** : parsing typé et sûr |
| Erreurs ignorées (rejets perdus) | **Either[Error, A]** : routage explicite |
| Appels réseau sans filet | **Try** : isolation des exceptions Java |
| Tout en mémoire (RAM) | **Doobie + H2** : persistance réelle |
| Code synchrone bloquant | **Future** : parallélisme moderne |

---

# 🧠 Grand Quiz du Mois 2

1. Pourquoi préférer une `case class` à un `Tuple` ?
2. Que garantit le mot-clé `sealed` au développeur ?
3. Comment faire communiquer une liste Java avec un filtre Scala ?
4. Quelle est la règle d'or d'un clearing équilibré ?

---

# 📝 Conclusion

Félicitations pour ce deuxième mois intensif ! Tu maîtrises maintenant l'essentiel du développement Scala en entreprise. Tu es capable de construire des briques métier complexes, sûres et performantes.

**Dernière étape** : La grande démo finale du Mois 2 !
