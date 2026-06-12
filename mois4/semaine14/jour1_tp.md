# TP Jour 1 : Mes premiers pas avec ZIO

**Durée :** ~4h | **Fil Rouge :** Un mini-système interactif

---

## Exercice 1 : Hello ZIO (1h)

1. Ajoute la dépendance `dev.zio :: zio` à ton projet (fictif).
2. Crée un objet `HelloZIO` qui étend `ZIOAppDefault`.
3. Utilise `Console.printLine` et `Console.readLine` pour demander le nom de l'internat et l'afficher.
4. Observe que le programme ne s'arrête que lorsque l'effet est exécuté par le "Runtime".

---

## Exercice 2 : Gestion d'Erreur Typée (1h30)

1. Crée un effet qui lit un nombre entier depuis la console.
2. Utilise `.mapError` pour transformer une éventuelle erreur de parsing en un message amical : "Ce n'est pas un nombre valide !".
3. Utilise `.catchAll` pour demander à nouveau si l'entrée est invalide.

---

## Exercice 3 : Parallélisme ZIO (1h30)

1. Crée deux effets `ZIO.sleep(1.second) *> Console.printLine("Task 1 Done")`.
2. Utilise l'opérateur `zipPar` (ou `&&&`) pour les lancer en parallèle.
3. Vérifie que le programme total prend 1 seconde et non 2.

**Livrable** : Code source d'un programme ZIO interactif gérant les erreurs et le parallélisme.
