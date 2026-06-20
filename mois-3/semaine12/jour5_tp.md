# TP Jour 5 : Clearing Engine v2.3 — La Démo Indestructible

**Durée :** ~4h | **Fil Rouge :** Démonstration finale de la maturité fonctionnelle

---

## Exercice 1 : Intégration Finale (2h)

1. Ton application `ClearingAppV23` doit assembler toutes les briques du Mois 3 :
   - Cœur pur et immuable.
   - Pipeline Railway (Either/Try).
   - Sérialisation via Type Classes (JSON/CSV/XML).
   - Logging monadique pur (`MonadicLogger`).
2. Lance un batch complet et affiche le résultat final + l'historique des logs accumulés.

> 💡 Utilise un `for-yield` unique pour orchestrer les étapes (parse, validate, net, export). C'est la synthèse parfaite de la semaine.

---

## Exercice 2 : Session de Stress-Test (1h30)

1. Lance tes propriétés ScalaCheck devant ton tuteur.
2. Explique pourquoi le succès de ces tests donne plus de confiance qu'un simple test unitaire.
3. Montre la version "Opaque Types" de ton domaine et explique pourquoi elle est performante (zéro allocation JVM grâce à l'effacement).

---

## Exercice 3 : Revue Technique & Auto-Évaluation (30 min)

1. Comparaison avant/après : montre un extrait de code du Mois 1 et le même extrait en Mois 3.
2. Commente l'évolution de la lisibilité et de la robustesse.
3. Remplis le tableau d'auto-évaluation ci-dessous :

| Concept                 | Confiance (1-5) | Point d'amélioration                  |
|-------------------------|-----------------|---------------------------------------|
| Functor (map)           |                 |                                       |
| Monad (flatMap + pure)  |                 |                                       |
| For-Comprehension       |                 |                                       |
| ScalaCheck (Gen, forAll)|                 |                                       |
| Opaque Types            |                 |                                       |
| Type Classes (given)    |                 |                                       |

**Bilan Mois 3** : Objectifs dépassés. Le stagiaire possède un socle théorique et pratique de haut niveau. Prêt pour la phase de mise à l'échelle (Mois 4).

**Livrable** : Code source complet v2.3, rapport de tests ScalaCheck et tag Git `v2.3.0`.
