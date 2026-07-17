package clearing.v23

final case class MonadicLogger[A](value: A, logs: List[String]):
  def map[B](f: A => B): MonadicLogger[B] =
    flatMap(value => MonadicLogger.pure(f(value)))

  def flatMap[B](f: A => MonadicLogger[B]): MonadicLogger[B] =
    val next = f(value)
    MonadicLogger(next.value, logs ++ next.logs)

object MonadicLogger:
  def pure[A](value: A): MonadicLogger[A] =
    MonadicLogger(value, Nil)

  given Monad[MonadicLogger] with
    def pure[A](value: A): MonadicLogger[A] =
      MonadicLogger.pure(value)

    extension [A](fa: MonadicLogger[A])
      def flatMap[B](f: A => MonadicLogger[B]): MonadicLogger[B] =
        fa.flatMap(f)
