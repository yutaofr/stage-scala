# TP Jour 5 : Clearing Engine v1.2 — Le Moteur Industriel

**Durée :** ~4h | **Fil Rouge :** Assemblage du moteur à haute performance

---

## Exercice 1 : Intégration du Netting Multilatéral (2h)

1. Intègre l'objet `MultilateralNetting` dans ton application principale.
2. Le moteur doit maintenant produire un rapport de règlement trié par importance (les plus gros débiteurs en premier).
3. Ajoute la vérification automatique de l'équilibre à la fin de chaque batch.

---

## Exercice 2 : Automatisation des Rapports Business (1h30)

1. Crée un `BusinessReporter` qui affiche :
   - Le volume total échangé.
   - La banque la plus active.
   - Les alertes de fenêtrage (fraud detection) rencontrées pendant le process.
2. Utilise `partition` pour séparer les logs techniques des résultats financiers.

---

## Exercice 3 : Revue de Code Finale S7 (30 min)

1. Prépare une démonstration du moteur sur 100 000 transactions.
2. Explique tes choix de structures de données (`Vector` vs `List`) à ton tuteur.

**Bilan Mois 2 - Semaine 7** : Le cœur financier de l'application est terminé. Nous avons une logique robuste, testable et performante.

**Livrable** : Code source complet v1.2, performance validée, et tests unitaires exhaustifs.
