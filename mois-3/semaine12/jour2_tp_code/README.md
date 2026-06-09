# Guide détaillé — TP Jour 2 : Ma Première Monade (`MonadicLogger`)

> **Mois 3 · Semaine 12 · Jour 2** — Durée ~4h
> **Fil rouge :** tracer le *clearing* de façon **pure** (sans `println`, sans effet de bord).
> Ce guide complète [`../jour2_tp.md`](../jour2_tp.md) avec plus de précision, les pièges, et les résultats attendus.

---

## 0. L'idée en une phrase

Une **Monade** = un **Functor** (`map`) + `pure` (mettre une valeur dans la boîte) + `flatMap` (chaîner deux boîtes).
Ici on construit une monade particulière, le **Writer** (`MonadicLogger`), qui **calcule une valeur tout en accumulant un journal**. Le journal voyage *dans le type de retour* au lieu d'être craché sur la sortie standard.

```
println("étape 1")   ← effet de bord, impur, intestable
   vs.
MonadicLogger(valeur, List("étape 1"))   ← le log fait partie de la valeur, pur
```

---

## 1. Comment lancer le projet

Outils : **Scala CLI** (déjà installé). Depuis ce dossier (`jour2_tp_code/`) :

```bash
# Vérifier que tout compile (sans exécuter) :
scala-cli compile .

# Lancer une démo précise (il y a plusieurs @main, il faut donc choisir) :
scala-cli run . --main-class demoEx1
scala-cli run . --main-class demoEx2
scala-cli run . --main-class demoEx3
```

> Tant qu'une méthode contient `???`, le code **compile** mais lève une
> `NotImplementedError` à l'exécution. C'est normal : c'est ton "bandeau rouge"
> à faire passer au vert.

---

## 2. Carte des fichiers

| Fichier | Exercice | Ce qui est fourni | **Ce que TU écris** |
|---|---|---|---|
| [`MonadicLogger.scala`](MonadicLogger.scala) | 1 & 3 | la case class, `pure`, la coquille du `given` | `map`, `flatMap`, et le corps du `given Monad` |
| [`Pipeline.scala`](Pipeline.scala) | 2 | `Transaction`, `logParse` (exemple), la démo | `logValidate` |
| [`Functor.scala`](Functor.scala) | 3 | le `Functor` du Jour 1 (rappel) | — |
| [`Monad.scala`](Monad.scala) | 3 | le trait `Monad`, la démo | le corps de `chain` |
| [`Main.scala`](Main.scala) | 1 | la démo `demoEx1` | — |

---

## 3. Exercice 1 — La structure `MonadicLogger` (~1h)

**Fichier :** [`MonadicLogger.scala`](MonadicLogger.scala)

### À écrire

```scala
def map[B](f: A => B): MonadicLogger[B] = ???
def flatMap[B](f: A => MonadicLogger[B]): MonadicLogger[B] = ???
```

### `map` — transforme la valeur, garde les logs
Un `map` ne raconte **rien de neuf**. Il change la valeur portée et **recopie les logs à l'identique**. Aucune nouvelle trace.

### `flatMap` — LE cœur de la monade
`f(value)` produit une **nouvelle boîte** (avec sa propre valeur ET ses propres logs). Tu dois renvoyer :
- la **valeur** produite par `f`,
- le **journal courant `++` le journal produit par `f`**.

### ⭐ Le choix de design qui compte : l'ordre des logs
| Ordre | Lecture du journal |
|---|---|
| `this.logs ++ nouvelle.logs` | **chronologique** (le passé d'abord) ✅ |
| `nouvelle.logs ++ this.logs` | anti-chronologique ❌ |

Pour tracer un pipeline étape par étape, on veut l'ordre **chronologique**. C'est ce que la démo attend.

### 💡 Bonus (la "preuve gratuite")
Une fois `flatMap` + `pure` en place, `map` peut se **dériver** :
```scala
def map[B](f: A => B): MonadicLogger[B] = flatMap(a => MonadicLogger.pure(f(a)))
```
C'est exactement ce que fait le trait `Monad` à l'Exercice 3. Écris-le d'abord "à la main" pour comprendre, puis observe que la dérivation donne le même résultat.

### Résultat attendu (`demoEx1`)
```
Valeur finale : 29
Journal :
  - départ avec 10
  - doublé 15 -> 30
  - décrémenté 30 -> 29
```

---

## 4. Exercice 2 — Utilisation dans le pipeline (~1h30)

**Fichier :** [`Pipeline.scala`](Pipeline.scala)

`logParse` t'est donné **comme exemple** du patron à suivre :
> *fais le travail → emballe `(résultat, List(trace))` dans un `MonadicLogger`.*

### À écrire
```scala
def logValidate(tx: Transaction): MonadicLogger[Transaction] = ???
```
Reproduis le patron : renvoie la transaction + `List("Validation OK")`.

### 💡 Point de design : un Writer n'échoue pas
`MonadicLogger` n'a **pas de canal d'échec** (contrairement à `Either` de la S10).
- Si une validation échoue, tu ne peux pas *court-circuiter* — tu peux seulement **loguer** un avertissement et laisser passer la valeur.
- La validation **bloquante** est le rôle d'`Either` ; le Writer ne fait qu'**accumuler de l'information**.

C'est *la* différence sémantique entre deux monades qui partagent le **même `flatMap`** :
| Monade | Ce que `flatMap` fait |
|---|---|
| `Either` | court-circuite au **premier échec** |
| `MonadicLogger` (Writer) | **accumule toujours** tout |

### La magie : le chaînage
```scala
logParse("TX1, ACC1, 100.0").flatMap(logValidate)
```
Tu n'écris **nulle part** "fusionne les logs" ici — c'est ton `flatMap` de l'Ex1 qui le fait tout seul. C'est ça, la composition monadique.

### Résultat attendu (`demoEx2`)
```
Transaction : Transaction(TX1,ACC1,100.0)
Journal :
  - Parsing OK : TX1
  - Validation OK
```

---

## 5. Exercice 3 — La type class `Monad` (~1h30)

**Fichiers :** [`Monad.scala`](Monad.scala) + le `given` dans [`MonadicLogger.scala`](MonadicLogger.scala)

On passe du **concret** (la classe `MonadicLogger`) à l'**abstrait** (la type class `Monad[F[_]]`), pour écrire du code qui marche sur **n'importe quelle** monade.

### 3.1 — Le trait (fourni)
```scala
trait Monad[F[_]] extends Functor[F]:
  def pure[A](a: A): F[A]
  extension [A](fa: F[A])
    def flatMap[B](f: A => F[B]): F[B]
    override def map[B](f: A => B): F[B] = fa.flatMap(a => pure(f(a)))  // dérivé !
```

### 3.2 — L'instance `given` (à toi, dans le companion de `MonadicLogger`)
```scala
given Monad[MonadicLogger] with
  def pure[A](a: A): MonadicLogger[A] = ???               // -> MonadicLogger.pure(a)
  extension [A](fa: MonadicLogger[A])
    def flatMap[B](f: A => MonadicLogger[B]) = ???         // -> fa.flatMap(f)
```

> ⚠️ **Piège "récursion infinie"** : dans le `given`, `fa.flatMap(f)` n'appelle **pas** l'extension qu'on est en train de définir. En Scala 3, une **méthode de la classe** (ton `flatMap` de l'Ex1) a **priorité** sur une extension method. Donc `fa.flatMap(f)` route bien vers l'Ex1. ✅

> 💡 **Pourquoi dans le companion ?** Le compilateur cherche un `Monad[MonadicLogger]` dans l'*implicit scope*, qui inclut le companion object de `MonadicLogger`. Il le trouve donc **automatiquement**. `import MonadicLogger.given` est explicite mais pas obligatoire ici.

### 3.3 — La fonction générique `chain` (à toi)
```scala
def chain[F[_], A, B, C](fa: F[A], f1: A => F[B], f2: B => F[C])(using Monad[F]): F[C] =
  ???   // -> fa.flatMap(f1).flatMap(f2)
```
Le `using Monad[F]` "débloque" `.flatMap` sur `fa` même si `F` est **abstrait**. C'est tout l'intérêt : `chain` ne sait pas si `F` est `MonadicLogger`, `Option` ou `Either` — il marche pour tous.

### Résultat attendu (`demoEx3`)
```
Transaction finale : Transaction(TX1,ACC1,100.0)
Journal d'orchestration :
  - Lecture de la ligne brute
  - Parsing OK : TX1
  - Validation OK
```

---

## 6. Pièges récurrents (à garder sous les yeux)

| Symptôme | Cause | Remède |
|---|---|---|
| `given` introuvable | type trop précis (`Some` au lieu de `Option`) | annote le type large |
| récursion infinie supposée | confusion méthode vs extension | la **méthode de classe gagne** (Scala 3) |
| logs dans le désordre | mauvais sens du `++` | `this.logs ++ nouveaux` |
| collision de noms | un autre `Logger` existe | nom explicite `MonadicLogger` |
| plusieurs `@main` → erreur | Scala CLI ne sait pas lequel | `--main-class demoExN` |

---

## 7. Bonus — Les 3 lois de la Monade (préparation au Jour 4)

Comme le Functor a ses lois (identité, composition), la Monade a les siennes. Le Jour 4 (ScalaCheck) les **prouvera par property-based testing** :

1. **Identité à gauche :** `pure(a).flatMap(f) == f(a)`
2. **Identité à droite :** `m.flatMap(pure) == m`
3. **Associativité :** `m.flatMap(f).flatMap(g) == m.flatMap(a => f(a).flatMap(g))`

Vérifie-les mentalement sur `MonadicLogger` : elles doivent tenir **exactement parce que** ton `flatMap` concatène les logs dans le bon ordre. C'est le pont vers le TP du Jour 4.

---

## 8. Checklist du livrable

- [ ] `map` et `flatMap` implémentés (`demoEx1` affiche `29` + 3 logs ordonnés)
- [ ] `logValidate` implémenté (`demoEx2` affiche 2 logs)
- [ ] `given Monad[MonadicLogger]` + `chain` implémentés (`demoEx3` affiche 3 logs)
- [ ] `scala-cli compile .` ne renvoie aucune erreur
- [ ] Tu sais expliquer : *pourquoi une Monade est toujours un Functor* et *pourquoi le Writer n'échoue pas*
