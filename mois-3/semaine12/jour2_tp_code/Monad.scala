/** Exercice 3 — La type class `Monad`.
  *
  * Une Monade = un Functor (`map`) + deux capacités :
  *   - `pure`    : mettre une valeur nue dans le contexte F
  *   - `flatMap` : enchaîner deux calculs contextualisés
  *
  * Point d'élégance du cours J2 : on n'implémente QUE `pure` + `flatMap`,
  * et `map` se DÉRIVE automatiquement. C'est la preuve que toute Monade
  * est un Functor.
  */
trait Monad[F[_]] extends Functor[F]:

  def pure[A](a: A): F[A]

  extension [A](fa: F[A])
    def flatMap[B](f: A => F[B]): F[B]

    // `map` dérivé : on emballe le résultat de f avec `pure`, et on chaîne
    // via `flatMap`. Fourni ici car c'est la "preuve" vue en cours.
    override def map[B](f: A => B): F[B] = fa.flatMap(a => pure(f(a)))

/** Exercice 3.3 — Une fonction GÉNÉRIQUE qui orchestre 3 étapes
  * pour N'IMPORTE QUELLE monade F (pas seulement MonadicLogger).
  *
  *   fa : la valeur de départ, dans le contexte F
  *   f1 : étape 1, transforme A en F[B]
  *   f2 : étape 2, transforme B en F[C]
  *
  * Le `using Monad[F]` rend disponibles `pure`/`flatMap`/`map` sur `fa`.
  */
def chain[F[_], A, B, C](fa: F[A], f1: A => F[B], f2: B => F[C])(using Monad[F]): F[C] =
  ??? // TODO (à toi) : enchaîne fa avec f1 puis f2 -> fa.flatMap(f1).flatMap(f2)

/** Démo Exercice 3 : on réutilise logParse/logValidate de l'Ex2,
  * mais orchestrés par la fonction générique `chain`.
  *
  * Lance avec :  scala-cli run . --main-class demoEx3
  */
@main def demoEx3(): Unit =
  import MonadicLogger.given // explicite (en réalité déjà trouvé via le companion)

  val depart: MonadicLogger[String] =
    MonadicLogger("TX1, ACC1, 100.0", List("Lecture de la ligne brute"))

  // A = String, B = Transaction, C = Transaction (types inférés)
  val resultat: MonadicLogger[Transaction] =
    chain(depart, logParse, logValidate)

  println(s"Transaction finale : ${resultat.value}")
  println("Journal d'orchestration :")
  resultat.logs.foreach(ligne => println(s"  - $ligne"))

  // Attendu une fois Ex1 + Ex2 + Ex3 implémentés :
  //   Transaction finale : Transaction(TX1,ACC1,100.0)
  //   Journal d'orchestration :
  //     - Lecture de la ligne brute
  //     - Parsing OK : TX1
  //     - Validation OK
