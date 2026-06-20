---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 20"
footer: "Jour 1 — Polissage Final & Bugfix"
---

# La touche finale
## Transformer un candidat en livraison vérifiée

**Durée :** ~2h | **Fil Rouge :** Aucun défaut bloquant connu, warnings triés

---

# 📋 Objectifs du Jour

- Éliminer les derniers avertissements de compilation (Warnings).
- Vérifier la cohérence de la documentation (README).
- S'assurer que les données de test sont "propres" et réalistes.
- Effectuer un "Smoke Test" complet sur l'environnement Docker.

---

# 1. Pourquoi le polissage ?

Le jury juge non seulement le fonctionnement, mais aussi la **Rigueur**.
- Un warning caché peut être le signe d'un bug futur.
- Un README mal formaté donne une impression de travail bâclé.

---

# 2. La checklist de livraison

1. **Compilation** : `sbt clean compile` -> 0 erreur, 0 warning.
2. **Tests** : `sbt test` -> 100% Green.
3. **Docker** : `docker compose down -v && docker compose up -d --wait` -> démarre sans erreur.
4. **Logs** : Les logs sont lisibles et ne contiennent pas de stacktraces inutiles.

---

# 3. Données de démonstration

Utilise des banques fictives ou des noms explicitement autorisés, des montants cohérents et aucune donnée personnelle. La démonstration doit rester crédible sans ressembler à une donnée de production.

---

# 🏗️ Application : Le Moteur Étincelant

Nous allons passer au peigne fin notre projet. Nous corrigerons les derniers types `Any` ou `null` qui auraient pu traîner, et nous enrichirons le `README.md` avec des instructions d'installation claires pour que le jury puisse le faire tourner chez lui.

---

# 🧠 Quiz Rapide

1. Un warning `deprecated` bloque-t-il toujours la livraison ? (Non : il doit être trié, documenté et corrigé selon son risque et l'échéance de suppression).
2. Que permet de tester un "Smoke Test" ? (Il vérifie que les fonctionnalités de base "ne fument pas" dès le démarrage).
3. Pourquoi utiliser des noms de banques réels ? (Pour l'immersion métier et la crédibilité de la solution).

---

# 📝 Résumé du Jour

- La qualité est dans les détails.
- Ton application est maintenant un "Produit Fini".
- Tu as l'esprit tranquille pour la suite de la semaine.

**Prochaine étape** : Utiliser le Kit 20.1 dans le TP du Jour 1.
