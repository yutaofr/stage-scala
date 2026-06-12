---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 20"
footer: "Jour 1 — Polissage Final & Bugfix"
---

# La Touche Finale
## Rien ne doit être laissé au hasard

**Durée :** ~2h | **Fil Rouge :** Zéro Bug, Zéro Warning

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

# 2. La Check-list Ultime

1. **Compilation** : `sbt clean compile` -> 0 erreur, 0 warning.
2. **Tests** : `sbt test` -> 100% Green.
3. **Docker** : `docker-compose down -v && docker-compose up` -> Démarre sans erreur.
4. **Logs** : Les logs sont lisibles et ne contiennent pas de stacktraces inutiles.

---

# 3. Réalité des Données

Utilise des noms de banques réels (Attijari, BCP, BMCE, CIH) et des montants cohérents. Cela rend ta démo beaucoup plus percutante pour l'équipe ATH.

---

# 🏗️ Application : Le Moteur Étincelant

Nous allons passer au peigne fin notre projet. Nous corrigerons les derniers types `Any` ou `null` qui auraient pu traîner, et nous enrichirons le `README.md` avec des instructions d'installation claires pour que le jury puisse le faire tourner chez lui.

---

# 🧠 Quiz Rapide

1. Est-ce grave d'avoir un warning "deprecated" ? (Oui, car cela signifie que ton code ne sera plus compatible avec les versions futures).
2. Que permet de tester un "Smoke Test" ? (Il vérifie que les fonctionnalités de base "ne fument pas" dès le démarrage).
3. Pourquoi utiliser des noms de banques réels ? (Pour l'immersion métier et la crédibilité de la solution).

---

# 📝 Résumé du Jour

- La qualité est dans les détails.
- Ton application est maintenant un "Produit Fini".
- Tu as l'esprit tranquille pour la suite de la semaine.

**Prochaine étape** : Polir ton projet dans le TP 96 !
