package clearing.v23

trait Functor[F[_]]:
  extension [A](fa: F[A])
    def map[B](f: A => B): F[B]

object Functor:
  def transform[F[_], A, B](
    fa: F[A],
    f: A => B
  )(using functor: Functor[F]): F[B] =
    functor.map(fa)(f)

  given Functor[List] with
    extension [A](values: List[A])
      def map[B](f: A => B): List[B] = values.map(f)

  given Functor[Option] with
    extension [A](value: Option[A])
      def map[B](f: A => B): Option[B] = value.map(f)

final case class Box[A](value: A)

object Box:
  given Functor[Box] with
    extension [A](box: Box[A])
      def map[B](f: A => B): Box[B] = Box(f(box.value))
