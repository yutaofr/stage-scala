/** Démo exécutable de l'Exercice 1.
  *
  * Lance-la avec :  scala-cli run . --main-class demoEx1
  * (depuis le dossier jour2_tp_code)
  *
  * Tant que map/flatMap contiennent `???`, ça compilera mais lèvera une
  * NotImplementedError à l'exécution. Une fois que tu les as implémentés,
  * tu dois voir la valeur finale ET les 3 lignes de journal dans l'ordre.
  */
@main def demoEx1(): Unit =
  val resultat =
    MonadicLogger(10, List("départ avec 10"))   // valeur 10, déjà 1 trace
      .map(_ + 5)                               // map : valeur -> 15, logs inchangés
      .flatMap(n => MonadicLogger(n * 2, List(s"doublé $n -> ${n * 2}")))
      .flatMap(n => MonadicLogger(n - 1, List(s"décrémenté $n -> ${n - 1}")))

  println(s"Valeur finale : ${resultat.value}")
  println("Journal :")
  resultat.logs.foreach(ligne => println(s"  - $ligne"))

  // Ce qu'on ATTEND une fois map/flatMap implémentés :
  //   Valeur finale : 29
  //   Journal :
  //     - départ avec 10
  //     - doublé 15 -> 30
  //     - décrémenté 30 -> 29
