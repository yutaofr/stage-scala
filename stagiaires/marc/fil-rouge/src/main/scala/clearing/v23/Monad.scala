package clearing.v23

trait Monad[F[_]] extends Functor[F]:
  def pure[A](value: A): F[A]

  extension [A](fa: F[A])
    def flatMap[B](f: A => F[B]): F[B]

    override def map[B](f: A => B): F[B] =
      fa.flatMap(value => pure(f(value)))

object Monad:
  def chain[F[_], A, B, C](
    fa: F[A],
    first: A => F[B],
    second: B => F[C]
  )(using monad: Monad[F]): F[C] =
    monad.flatMap(fa): value =>
      monad.flatMap(first(value))(second)
