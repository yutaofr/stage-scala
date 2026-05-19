# TP Jour 1 : La Chasse aux Effets de Bord

**Durée :** ~4h | **Fil Rouge :** Assainir le moteur v1.3

---

## Exercice 1 : Audit de Pureté (1h)

1. Relis l'intégralité du code de ton projet (version 1.3).
2. Liste tous les endroits où tu utilises :
   - `println`.
   - `throw new Exception`.
   - `java.time.LocalDateTime.now()`.
   - Des appels `HttpClient`.
3. Pour chaque point, note pourquoi il viole la Transparence Référentielle.

---

## Exercice 2 : Isolation des I/O (1h30)

1. Crée un objet `ClearingReporter` dédié uniquement à l'affichage.
2. Déplace TOUS les `println` de ton moteur vers cet objet.
3. Les autres fonctions doivent maintenant retourner des `String` ou des types de données, mais ne plus jamais imprimer elles-mêmes.

---

## Exercice 3 : Découplage du Temps (1h30)

1. Identifie les fonctions qui utilisent `LocalDateTime.now()`.
2. Modifie-les pour qu'elles acceptent le temps comme un argument : `def process(tx: Transaction, now: LocalDateTime)`.
3. Vérifie que tu peux maintenant tester ces fonctions avec une date fixe (Test déterminisme).

**Livrable** : Rapport d'audit + Code refactorisé où la logique de calcul ne contient plus d'I/O ni de dépendance au temps système direct.
