---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 17"
footer: "Jour 1 — Les 3 Piliers de l'Observabilité"
---

# Observabilité
## Regarder à l'intérieur d'un système qui tourne

**Durée :** ~2h | **Fil Rouge :** Sortir du `println` pour des logs structurés

---

# 📋 Objectifs du Jour

- Comprendre la différence entre Monitoring et Observabilité.
- Découvrir trois signaux majeurs : logs, métriques et traces.
- Apprendre à utiliser **Logback** pour des logs professionnels.
- Transformer nos logs en JSON pour qu'ils soient lisibles par une machine.

---

# 1. Pourquoi l'Observabilité ?

En local, vous voyez la console. En production (sur 10 serveurs), vous ne pouvez pas "regarder" la console.
L'observabilité est la capacité de comprendre l'état interne du système uniquement à partir des données qu'il expose à l'extérieur.

---

# 2. Trois signaux complémentaires

1. **Logs** : Événements discrets (ex: "Transaction X validée").
2. **Métriques** : Chiffres agrégés sur le temps (ex: "150 tx/sec", "CPU à 40%").
3. **Traces** : Le trajet d'une requête spécifique à travers tous les services.

---

# 3. Logs Structurés (JSON)

Un log texte libre est difficile à filtrer. Un log JSON est un événement structuré que les outils peuvent indexer.

### Avant (Mauvais) ❌
`INFO: Bank A sent 100 DH at 12:00`

### Après (Bon) ✅
```json
{
  "level": "INFO",
  "bank": "CIH",
  "amount": 100,
  "action": "DEPOSIT",
  "timestamp": "2024-03-21T12:00:00"
}
```

---

# 🏗️ Application : Logback & SLF4J

Nous allons produire des logs JSON avec un contexte compatible avec les Fibers. Un `MDC` basé uniquement sur le thread peut perdre la corrélation quand une Fiber change de thread ; ZIO Logging fournit un contexte fondé sur `FiberRef`.

---

# 🧠 Quiz Rapide

1. Pourquoi le JSON est-il meilleur que le format texte pour les logs ? (Parce qu'il permet de faire des recherches et des tableaux de bord facilement dans des outils comme ELK).
2. Quel pilier permet de savoir combien de transactions ont échoué en une heure ? (Les Métriques).
3. Quel pilier permet de savoir pourquoi UNE transaction spécifique a mis 5 secondes à passer ? (Les Traces).

---

# 📝 Résumé du Jour

- L'observabilité est indispensable pour la production.
- On ne "print" plus, on "log".
- Le format JSON est le standard de l'industrie.
- Ton application commence à parler le langage des Ops.

**Prochaine étape** : Utiliser le Kit 17.1 dans le TP du Jour 1.
