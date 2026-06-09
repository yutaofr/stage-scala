/** Exercice 2 — Brancher la monade sur le vrai pipeline de clearing.
  *
  * Objectif : deux étapes (parser, valider) qui calculent ET tracent.
  * En les chaînant avec `flatMap`, les traces s'ACCUMULENT toutes seules.
  */

/** Modèle minimal d'une transaction du moteur de clearing.
  * (id, compte concerné, montant signé) */
final case class Transaction(id: String, compte: String, montant: Double)

/** Exercice 2.1 — EXEMPLE FOURNI : le patron à reproduire.
  *
  * Parse une ligne CSV "id,compte,montant" en `Transaction`,
  * et émet UNE trace. Remarque le schéma : on fait le travail, puis on
  * emballe `(résultat, List(trace))` dans un MonadicLogger.
  */
def logParse(line: String): MonadicLogger[Transaction] =
  val parts = line.split(",").map(_.trim)
  val tx = Transaction(parts(0), parts(1), parts(2).toDouble)
  MonadicLogger(tx, List(s"Parsing OK : ${tx.id}"))

/** Exercice 2.2 — À TOI : valide la transaction et émet "Validation OK".
  *
  * Même patron que logParse : tu renvoies la transaction + UNE trace.
  *
  * 💡 Point de design (à méditer) : un MonadicLogger (Writer) n'a PAS de
  *    canal d'échec — contrairement à `Either`. Si le montant est invalide,
  *    tu ne peux pas "court-circuiter" : tu peux seulement LOGUER un
  *    avertissement et laisser passer la valeur. La validation "bloquante",
  *    c'est le rôle d'`Either` (S10), pas de Writer.
  *    Pour ce TP, contente-toi du cas nominal : trace "Validation OK".
  */
def logValidate(tx: Transaction): MonadicLogger[Transaction] =
  ??? // TODO (à toi) : renvoie tx + List("Validation OK")

/** Démo Exercice 2 : on CHAÎNE parse puis validate avec flatMap.
  *
  * Lance avec :  scala-cli run . --main-class demoEx2
  */
@main def demoEx2(): Unit =
  val resultat: MonadicLogger[Transaction] =
    logParse("TX1, ACC1, 100.0").flatMap(logValidate)

  println(s"Transaction : ${resultat.value}")
  println("Journal :")
  resultat.logs.foreach(ligne => println(s"  - $ligne"))

  // Attendu une fois Ex1 + Ex2 implémentés :
  //   Transaction : Transaction(TX1,ACC1,100.0)
  //   Journal :
  //     - Parsing OK : TX1
  //     - Validation OK
