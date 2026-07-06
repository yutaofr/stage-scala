# Starter Kit Semaine 14 : observer ZIO dans votre fil rouge

La semaine 14 part d'une hypothèse simple : chaque stagiaire possède **sa propre version** du moteur fil rouge, construite en suivant les TPs des semaines 1 à 13. Le support ne fournit donc pas un projet prêt à utiliser, ni un module complet à copier. Il donne une trame pour ajouter un seul petit module d'observation dans le projet du stagiaire.

## Contrat pédagogique

- Le stagiaire travaille dans **son** projet fil rouge.
- Les types et fonctions des semaines 1 à 13 restent la source de vérité.
- Le cœur Scala de base reste intact : validation, `Either`, types opaques, netting pur, tests.
- La semaine 14 ajoute un seul fichier d'observation, par exemple `ZioEffectObservation.scala`.
- Ce fichier observe le système d'effet ZIO. Il ne devient pas une nouvelle architecture.
- Chaque exercice dure 5 à 10 minutes.
- Si l'adaptation des noms prend plus de 10 minutes, le tuteur donne le mapping et passe à l'observation.

## Fiche de correspondance à remplir

Avant d'écrire ZIO, le stagiaire identifie ce qu'il a déjà construit.

| Élément attendu depuis S1-S13 | Dans mon projet |
|---|---|
| Type transaction | `...` |
| Type banque | `...` |
| Type montant | `...` |
| Type erreur métier, souvent un ADT | `...` |
| Fonction de validation pure | `... => Either[Erreur, Transaction]` |
| Fonction de netting pur | `List[Transaction] => PositionsNettes` |
| Petit batch de test déjà utilisé | `...` |
| Erreur technique ou infrastructure, si elle existe | `...` |

Cette table remplace l'idée d'un projet fourni par le support. Le starter kit s'appuie sur ce que le stagiaire a produit.

## Module d'observation, non productif

Nom conseillé : `ZioEffectObservation`. Le package dépend du projet du stagiaire.

```scala
// Exemple de chemin, à adapter :
// src/main/scala/<votre/package>/zio/ZioEffectObservation.scala

import zio.*

object ZioEffectObservation extends ZIOAppDefault:

  // Zone d'adaptation : remplacer ces alias par les types du projet S1-S13.
  type Tx = VotreTypeTransaction
  type Err = VotreTypeErreur
  type Positions = VotreTypePositionsNettes

  // Reprendre un batch minuscule déjà connu du stagiaire.
  // Deux transactions suffisent : le but est d'observer ZIO, pas de tester le métier.
  val sampleBatch: List[Tx] =
    votreBatchDeReference

  // Brancher les fonctions pures déjà écrites.
  // Scala de base reste responsable de la validation et du netting.
  def validateBase(tx: Tx): Either[Err, Tx] =
    votreValidationExistante(tx)

  def nettingBase(txs: List[Tx]): Positions =
    votreNettingExistant(txs)

  // Petit affichage local au module d'observation.
  // Il évite de modifier le reporting déjà construit.
  def showPositions(positions: Positions): String =
    positions.toString
```

Ce bloc n'est pas une solution prête à compiler. Il force le stagiaire à relier ZIO à **son** moteur.

## Guide de lecture des API ZIO utilisées

Garder cette section sous les yeux pendant les exercices. Elle explique les API présentes dans ce starter kit, dans leur contexte local. Le but n'est pas de tout apprendre sur ZIO, mais de savoir lire le code S14 sans deviner.

### Types et point d'entrée

| API | Dans le starter kit | À retenir |
|---|---|---|
| `import zio.*` | Importe `ZIO`, `IO`, `ZLayer`, `Schedule`, `Ref`, `Scope`, `Clock`, et la syntaxe comme `300.millis`. | Un seul import donne accès aux types principaux et aux petites extensions de durée. |
| `ZIOAppDefault` | `object ZioEffectObservation extends ZIOAppDefault` | Déclare une petite application ZIO. ZIO exécutera l'effet retourné par `run`. |
| `def run` | Point de départ du module d'observation. | On ne met pas le métier dans `main`; on décrit un programme ZIO dans `run`. |
| `ZIO[R, E, A]` | Exemple : `ZIO[ObservationConfig, Err, Positions]`. | `R` est ce que le programme demande, `E` est l'erreur prévue, `A` est le résultat produit. |
| `IO[E, A]` | Exemple : `IO[Err, Positions]`. | Raccourci pour `ZIO[Any, E, A]`. L'effet ne demande aucune dépendance. |
| `Any` dans `ZIO[Any, E, A]` | Utilisé dans `timed` et dans `Schedule[Any, Err, Any]`. | `Any` dans le canal `R` signifie : aucune dépendance à fournir. |
| `Nothing` dans `ZIO[Scope, Nothing, BufferedReader]` | Utilisé pour `openAuditReader`. | `Nothing` dans le canal `E` signifie : aucune erreur typée ne peut sortir de cet effet. |

### Créer, convertir et enchaîner des effets

| API | Dans le starter kit | À retenir |
|---|---|---|
| `ZIO.suspendSucceed(basePipeline)` | Suspend le pipeline Scala de base avant `fromEither`. | Le calcul n'est pas lancé au moment où la valeur est déclarée. On l'exécute plus tard, dans ZIO. |
| `ZIO.fromEither(...)` | Transforme `Either[Err, A]` en `IO[Err, A]`. | `Left(err)` devient un échec ZIO typé; `Right(value)` devient une réussite ZIO. |
| `ZIO.succeed(...)` | Retourne une valeur sûre; dans ce starter minimal, il sert aussi aux `println` d'observation. | Ne pas l'utiliser pour une opération qui peut échouer de façon prévue. Dans ce cas, préférer `ZIO.attempt`. |
| `ZIO.attempt(...)` | Encadre `reader.readLine()` et la fermeture du reader. | Capture une exception Java/Scala dans le canal d'erreur typé de ZIO. |
| `.mapError(_.getMessage)` | Convertit une exception de lecture en `String`. | Change le type d'erreur sans toucher au succès. |
| `ZIO.fail(...)` | Crée l'erreur volontaire de lecture ou l'échec temporaire de publication. | Produit un échec attendu dans le canal `E`, pas une exception cachée. |
| `.flatMap(...)` | Enchaîne `suspendSucceed` puis `fromEither`; enchaîne aussi la boucle de lecture. | Lance le second effet seulement après le premier, avec son résultat. Une `for`-comprehension compile souvent vers `flatMap`. |
| `*>` | Affiche une ligne puis continue la boucle : `ZIO.succeed(...) *> loop(...)`. | Exécute deux effets dans l'ordre et ignore le résultat du premier. |
| `.either` | Utilisé dans `run` pour `readAuditLines`, `businessFailureObservation`, et `publishObservation`. | Transforme un échec ZIO en valeur `Left(error)`, ce qui permet de continuer l'observation au lieu d'arrêter le programme. |

### Dépendances locales avec `R`

| API | Dans le starter kit | À retenir |
|---|---|---|
| `ZIO.service[ObservationConfig]` | Lit `parallelism` et `failAuditAfterFirstLine`. | Demande une dépendance présente dans le canal `R`. Ici, la dépendance est une simple configuration locale. |
| `ZLayer.succeed(defaultConfig)` | Fournit la configuration au programme final. | Crée une couche très simple à partir d'une valeur déjà disponible. |
| `.provide(...)` | `program.provide(ZLayer.succeed(defaultConfig))`. | Branche les dépendances demandées par `R`. Après ce branchement, le programme peut être exécuté. |

### Ressources et fermeture garantie

| API | Dans le starter kit | À retenir |
|---|---|---|
| `Scope` | Requis par `openAuditReader`. | Représente la zone dans laquelle une ressource reste ouverte. Quand le `Scope` se ferme, ZIO lance les finalizers. |
| `ZIO.acquireRelease(acquire)(release)` | Ouvre puis ferme le `BufferedReader`. | Si l'acquisition réussit, la libération sera appelée en succès, en échec, ou en interruption. |
| `.orDie` | Utilisé dans le finalizer de fermeture. | Convertit une erreur de fermeture non récupérable en défaut ZIO. Le finalizer ne doit pas ajouter une nouvelle erreur métier au TP. |
| `ZIO.scoped { ... }` | Encadre l'ouverture et la lecture de l'audit. | Crée un `Scope`, exécute le bloc, puis ferme les ressources acquises dans ce bloc. |

### Temps, parallèle et état d'observation

| API | Dans le starter kit | À retenir |
|---|---|---|
| `Clock.nanoTime` | Mesure le début et la fin dans `timed`. | Mesure un temps monotone dans un effet ZIO. C'est adapté pour une durée, pas pour afficher une date métier. |
| `100.millis`, `300.millis` | Durées pour `sleep` et `Schedule.exponential`. | Syntaxe de durée fournie par `zio.*`. |
| `ZIO.sleep(...)` | Ralentit volontairement `validateSlow`. | Suspend la fiber sans bloquer un thread comme `Thread.sleep`. |
| `ZIO.foreach(list)(f)` | Valide le batch séquentiellement. | Traite les éléments dans l'ordre, un effet après l'autre. |
| `ZIO.foreachPar(list)(f)` | Valide le batch en parallèle. | Lance plusieurs effets en parallèle avec des fibers ZIO. |
| `.withParallelism(config.parallelism)` | Limite `foreachPar`. | Garde le parallélisme borné. Le stagiaire voit l'effet de la configuration sans créer de threads à la main. |
| `Ref[Int]` | Type du compteur passé à `publishObservation`. | Une `Ref` garde un état mutable contrôlé par ZIO. Ici, elle compte les tentatives. |
| `Ref.make(0)` | Crée le compteur de tentatives. | `Ref` est une petite référence mutable sûre pour les fibers. Elle sert ici à observer les retries. |
| `counter.updateAndGet(_ + 1)` | Incrémente et retourne le compteur. | La mise à jour est atomique du point de vue des fibers ZIO. |

### Retry et politique d'erreur

| API | Dans le starter kit | À retenir |
|---|---|---|
| `Schedule[Any, Err, Any]` | Type de `retryTemporaryOnly`. | Décrit une politique de répétition qui ne demande pas de dépendance, lit des erreurs `Err`, et ignore sa sortie. |
| `Schedule.exponential(100.millis)` | Attend de plus en plus longtemps entre les essais. | Évite de relancer immédiatement une opération temporairement indisponible. |
| `Schedule.recurs(3)` | Borne le nombre de reprises. | Empêche une boucle de retry infinie. |
| `schedule1 && schedule2` | Combine backoff exponentiel et nombre maximal de reprises. | Les deux règles s'appliquent ensemble : délai progressif et limite de tentatives. |
| `.whileInput(isTemporary)` | Filtre les erreurs retentables. | La politique continue seulement pour les erreurs considérées temporaires. |
| `.retry(retryTemporaryOnly)` | Retente la publication, mais pas l'erreur métier. | ZIO relance l'effet seulement quand il échoue et que le `Schedule` accepte l'erreur. |

## Jour 1 : effet et comparaison Scala de base

```scala
  // Scala de base : le pipeline existe déjà avec Either.
  // def garde le calcul réexécutable pour l'observation.
  def basePipeline: Either[Err, Positions] =
    for
      valid <- sampleBatch.foldRight(Right(Nil): Either[Err, List[Tx]]) { (tx, acc) =>
        for
          checked <- validateBase(tx)
          rest    <- acc
        yield checked :: rest
      }
    yield nettingBase(valid)

  // ZIO : on suspend le pipeline Scala de base dans une description.
  // Intérêt : le travail ne se lance pas quand la valeur est déclarée.
  val zioPipeline: IO[Err, Positions] =
    ZIO.suspendSucceed(basePipeline).flatMap(result => ZIO.fromEither(result))
```

Observation attendue : le résultat métier est le même. ZIO ne remplace pas la logique; il décrit son exécution.

## Jour 2 : dépendance locale avec `R`

```scala
  final case class ObservationConfig(parallelism: Int, failAuditAfterFirstLine: Boolean)

  val defaultConfig: ObservationConfig =
    ObservationConfig(parallelism = 2, failAuditAfterFirstLine = false)

  // Le programme demande maintenant une configuration.
  // R vaut ObservationConfig; ce n'est pas un nouveau service métier.
  val configuredPipeline: ZIO[ObservationConfig, Err, Positions] =
    for
      config    <- ZIO.service[ObservationConfig]
      positions <- zioPipeline
      _         <- ZIO.succeed(println(s"parallélisme observé = ${config.parallelism}"))
    yield positions
```

Observation attendue : sans `.provide(ZLayer.succeed(defaultConfig))`, le programme ne peut pas être lancé. Le canal `R` rend la dépendance visible.

## Jour 3 : ressource courte avec `Scope`

```scala
  import java.io.{BufferedReader, StringReader}

  val auditText: String =
    sampleBatch.map(_.toString).mkString("\n")

  def openAuditReader: ZIO[Scope, Nothing, BufferedReader] =
    ZIO.acquireRelease(
      ZIO.succeed {
        println("audit acquire")
        new BufferedReader(new StringReader(auditText))
      }
    ) { reader =>
      ZIO.attempt {
        reader.close()
        println("audit release")
      }.orDie
    }

  def readAuditLines(failAfterFirstLine: Boolean): IO[String, List[String]] =
    def loop(reader: BufferedReader, acc: List[String]): IO[String, List[String]] =
      ZIO.attempt(reader.readLine()).mapError(_.getMessage).flatMap {
        case null =>
          ZIO.succeed(acc.reverse)
        case line if failAfterFirstLine && acc.nonEmpty =>
          ZIO.fail(s"lecture arrêtée volontairement: $line")
        case line =>
          ZIO.succeed(println(s"audit read: $line")) *> loop(reader, line :: acc)
      }

    ZIO.scoped {
      for
        reader <- openAuditReader
        lines  <- loop(reader, Nil)
      yield lines
    }
```

Observation attendue : `audit release` apparaît en succès et en erreur. On observe `Scope`, pas un parseur CSV complet.

## Jour 4 : parallèle borné

```scala
  def timed[E, A](label: String)(effect: ZIO[Any, E, A]): ZIO[Any, E, String] =
    for
      start <- Clock.nanoTime
      value <- effect
      end   <- Clock.nanoTime
    yield s"$label: $value en ${(end - start) / 1000000} ms"

  def validateSlow(tx: Tx): IO[Err, Tx] =
    ZIO.sleep(300.millis) *> ZIO.fromEither(validateBase(tx))

  val parallelPreview: ZIO[ObservationConfig, Err, String] =
    for
      config     <- ZIO.service[ObservationConfig]
      sequential <- timed("séquentiel")(ZIO.foreach(sampleBatch)(validateSlow))
      parallel   <- timed("parallèle")(ZIO.foreachPar(sampleBatch)(validateSlow).withParallelism(config.parallelism))
    yield s"$sequential\n$parallel"
```

Observation attendue : le stagiaire voit la différence entre composition séquentielle et parallèle sans créer de `Thread`.

## Jour 5 : retry borné, sans nouveau domaine

```scala
  // Adapter ces deux fonctions au type d'erreur du projet.
  // Si le projet possède déjà une erreur technique, l'utiliser.
  def isTemporary(error: Err): Boolean =
    votrePredicatErreurTemporaire(error)

  def temporaryFailure(message: String): Err =
    votreErreurTechnique(message)

  val retryTemporaryOnly: Schedule[Any, Err, Any] =
    (Schedule.exponential(100.millis) && Schedule.recurs(3)).whileInput(isTemporary)

  def publishObservation(counter: Ref[Int], positions: Positions): IO[Err, String] =
    for
      attempt <- counter.updateAndGet(_ + 1)
      _       <- ZIO.succeed(println(s"publication observée #$attempt"))
      result  <-
        if attempt < 3 then ZIO.fail(temporaryFailure("journal temporairement indisponible"))
        else ZIO.succeed(s"publication observée: ${showPositions(positions)}")
    yield result

  // Construire une erreur métier de référence à partir du domaine du stagiaire.
  // Exemple : une banque inconnue, un montant négatif, ou une transaction mal formée.
  def businessFailureObservation: IO[Err, Unit] =
    val businessError: Either[Err, Unit] =
      votreErreurMetierDeReference

    ZIO.fromEither(businessError)
```

Observation attendue : une panne technique temporaire est retentée. Une erreur métier issue du domaine est passée dans la même politique de retry, mais elle n'est pas retentée. Le module ne crée pas de client HTTP, pas de circuit breaker, pas de nouvelle couche métier.

## Programme `run` minimal

```scala
  def run =
    val program =
      for
        positions <- configuredPipeline
        _         <- ZIO.succeed(println(showPositions(positions)))
        _         <- readAuditLines(failAfterFirstLine = false).either
        preview   <- parallelPreview
        _         <- ZIO.succeed(println(preview))
        business  <- businessFailureObservation.retry(retryTemporaryOnly).either
        _         <- ZIO.succeed(println(s"erreur métier non retentée: $business"))
        counter   <- Ref.make(0)
        publish   <- publishObservation(counter, positions).retry(retryTemporaryOnly).either
        _         <- ZIO.succeed(println(publish))
      yield ()

    program.provide(ZLayer.succeed(defaultConfig))
```

Le `run` sert à observer. Il n'est pas une nouvelle entrée officielle du moteur.

## Ce que le tuteur doit refuser

- Importer un projet fil rouge prêt à l'emploi depuis le support.
- Coller un module complet sans l'adapter aux noms du stagiaire.
- Réécrire la validation, le netting, ou les types métier en ZIO.
- Ajouter un vrai client HTTP, une base de données, un repository, ou un circuit breaker.
- Transformer la semaine 14 en migration ZIO du moteur.

La bonne phrase de recadrage : **"On observe ZIO autour de ton moteur existant; on ne remplace pas ton moteur."**
