/** Rappel du Jour 1 — la type class Functor.
  *
  * Un Functor, c'est "un conteneur sur lequel on peut faire `.map`".
  * On l'exprime en Scala 3 avec une `extension method` : n'importe quel
  * `F[A]` qui possède une instance `Functor[F]` gagne la méthode `.map`.
  *
  * On en a besoin ici car, à l'Exercice 3, la Monade ÉTEND le Functor :
  * toute Monade est automatiquement un Functor.
  */
trait Functor[F[_]]:
  extension [A](fa: F[A])
    def map[B](f: A => B): F[B]
