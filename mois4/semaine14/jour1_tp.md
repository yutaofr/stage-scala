# TP Jour 1 : Mes premiers pas avec ZIO

**Durée :** ~4h | **Fil Rouge :** Un mini-système interactif

---

## Point de départ

- Valide d'abord le **Kit 14.0**, puis copie le **Kit 14.1** de `starter_kit_s14.md`.
- Lance d'abord le squelette sans modification.
- À la fin de chaque exercice, relance le programme et conserve la sortie de validation.

## Exercice 1 : Hello ZIO (1h)

**Objectif :** construire un effet interactif sans lancer d'I/O pendant sa définition.

1. Vérifie la dépendance ZIO fournie dans le projet du kit.
2. Dans `ZioStarter`, demande le nom de la banque avec `Console.readLine`.
3. Nettoie la saisie avec `trim` et refuse une chaîne vide avec `ZIO.fail`.
4. Affiche un message de bienvenue, puis le nombre de transactions à traiter.
5. Repère la différence entre la valeur `clearingLogic` et son exécution par `run`.

**Validation :** une banque vide produit un message contrôlé ; une banque valide affiche le récapitulatif.

---

## Exercice 2 : Gestion d'Erreur Typée (1h30)

**Objectif :** convertir une exception de parsing en erreur métier, puis répéter proprement la saisie.

1. Crée `readTransactionCount: IO[InputError, Int]`.
2. Encapsule `toInt` avec `ZIO.attempt` puis transforme l'erreur avec `mapError`.
3. Refuse les valeurs négatives ou supérieures à `100000`.
4. Crée une boucle récursive `askUntilValid` qui affiche l'erreur puis redemande la valeur.
5. Teste successivement `abc`, `-1`, puis `10`.

**Validation :** le programme ne termine pas sur une entrée invalide et retourne finalement `10`.

---

## Exercice 3 : Parallélisme ZIO (1h30)

**Objectif :** mesurer la différence entre composition séquentielle et parallèle.

1. Crée deux effets qui attendent une seconde et renvoient un nom de batch.
2. Mesure la version séquentielle avec `Clock.nanoTime`.
3. Exécute les mêmes effets avec `zipPar`.
4. Compare les durées sans exiger une valeur exacte : environ 2 s en séquentiel, environ 1 s en parallèle.
5. Ajoute une assertion simple ou un message `PARALLELE_OK` si la seconde durée est inférieure à la première.

**Validation :** la sortie contient les deux résultats et montre un gain mesuré.

**Livrable** : programme ZIO interactif, sorties des trois cas de saisie et comparaison des deux durées.
