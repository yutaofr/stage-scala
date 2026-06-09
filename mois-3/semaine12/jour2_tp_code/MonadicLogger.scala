/** Exercice 1 — Ma première monade : MonadicLogger (la "Writer monad").
  *
  * Un MonadicLogger transporte DEUX choses en même temps :
  *   - `value` : la valeur calculée
  *   - `logs`  : le journal des traces accumulées jusqu'ici
  *
  * Tout l'intérêt : calculer EN accumulant des traces, sans aucun `println`,
  * de façon pure et immuable. Le journal voyage DANS le type de retour.
  *
  * NB : on l'appelle `MonadicLogger` (et pas juste `Logger`) pour éviter toute
  * collision de noms avec un éventuel logger d'infrastructure (cf. cours J2).
  */
final case class MonadicLogger[A](value: A, logs: List[String]):

  /** Exercice 1.2 — map : transformer la VALEUR, conserver les logs.
    *
    * Un simple `map` ne raconte rien de neuf : il transforme la valeur
    * portée, mais le journal reste exactement le même.
    *
    * Indice : la nouvelle boîte contient `f(value)` ... et `logs` à l'identique.
    */
  def map[B](f: A => B): MonadicLogger[B] =
    ??? // TODO (à toi) : transforme la valeur avec f, garde les mêmes logs

  /** Exercice 1.3 — flatMap : transformer la valeur ET fusionner les logs.
    *
    * Ici `f` ne renvoie pas une valeur nue, mais un NOUVEAU MonadicLogger,
    * avec sa propre valeur et ses propres logs. Le résultat doit contenir :
    *   - la valeur produite par `f`
    *   - les logs ACTUELS (`this.logs`) SUIVIS des logs produits par `f`,
    *     dans cet ordre (l'historique se lit de haut en bas).
    *
    * C'est CETTE fusion (`++`) qui fait toute la magie de l'accumulation.
    */
  def flatMap[B](f: A => MonadicLogger[B]): MonadicLogger[B] =
    ??? // TODO (à toi) : applique f à value, puis combine les deux journaux

object MonadicLogger:

  /** pure / unit : mettre une valeur "nue" dans la boîte, journal vide.
    * C'est le point de départ neutre d'une chaîne de calculs. */
  def pure[A](a: A): MonadicLogger[A] = MonadicLogger(a, Nil)

  /** Exercice 3.2 — L'instance de la type class `Monad` pour `MonadicLogger`.
    *
    * On la place DANS le companion object : ainsi le compilateur la trouve
    * automatiquement dès qu'il cherche un `Monad[MonadicLogger]`
    * (résolution par "implicit scope"). Un `import MonadicLogger.given`
    * fonctionne aussi explicitement.
    *
    * Tu n'as que DEUX choses à fournir : `pure` et `flatMap`.
    * Le `map` est dérivé gratuitement dans le trait `Monad` (cf. Monad.scala).
    */
  given Monad[MonadicLogger] with

    def pure[A](a: A): MonadicLogger[A] =
      ??? // TODO (à toi) : réutilise le pure du companion -> MonadicLogger.pure(a)

    extension [A](fa: MonadicLogger[A])
      def flatMap[B](f: A => MonadicLogger[B]): MonadicLogger[B] =
        ??? // TODO (à toi) : délègue à la méthode déjà écrite -> fa.flatMap(f)
        // ⚠️ Pas de récursion infinie ici : en Scala 3, une MÉTHODE de la classe
        //    (le flatMap de l'Exercice 1) a priorité sur une extension method.
        //    `fa.flatMap(f)` appelle donc bien ta méthode de l'Ex1, pas elle-même.
