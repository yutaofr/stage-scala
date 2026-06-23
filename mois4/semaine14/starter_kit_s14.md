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
      ZIO.succeed {
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
