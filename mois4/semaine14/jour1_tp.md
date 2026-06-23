# TP Jour 1 : Observer un premier programme ZIO

**Durée :** 30 min utiles | **Format :** 3 micro-exercices de 10 min maximum

---

## Point de départ

- Valide d'abord le **Kit 14.0**.
- Copie le **Kit 14.1** dans `src/main/scala/distributed/zio/ZioStarter.scala`.
- Lance le programme avant de modifier quoi que ce soit.

Le but n'est pas de construire une application interactive. Le but est de voir où ZIO place l'I/O, les erreurs, et le parallèle.

## Exercice 1 : La description n'exécute rien (8 min)

1. Lance `sbt "runMain distributed.zio.ZioStarter"`.
2. Observe les valeurs `rawBank = "AWB"` et `rawCount = "2"`.
3. Repère dans le code `askRequest`, `clearingLogic`, et `run`.
4. Écris une phrase : quelle valeur de code décrit le programme, et quelle valeur le lance ?

**Validation :** la sortie contient la banque, le nombre, une ligne `zip`, et une ligne `zipPar`.

---

## Exercice 2 : Lire une erreur typée (10 min)

1. Relance le programme.
2. Remplace `rawBank = "AWB"` par `rawBank = ""`.
3. Relance, puis remets `AWB` et remplace `rawCount = "2"` par `rawCount = "abc"`.
4. Relie chaque sortie à un cas de `InputError`.

**Validation :** l'erreur est affichée par `catchAll`; le programme ne montre pas de stack trace.

---

## Exercice 3 : Voir `zipPar` sans parler de threads (10 min)

1. Observe les durées imprimées pour `zip` et `zipPar`.
2. Relance avec `rawBank = "AWB"` et `rawCount = "2"`.
3. Dans `batchA` et `batchB`, remplace `300.millis` par `700.millis`.
4. Relance et compare : `zip` additionne les délais, `zipPar` garde environ le plus long.
5. Remets `300.millis` si le tuteur veut garder le kit initial.

**Validation :** le stagiaire sait dire : "`zipPar` compose deux effets en parallèle, donc la durée baisse."

**Livrable court :** trois sorties console et trois phrases de constat.
