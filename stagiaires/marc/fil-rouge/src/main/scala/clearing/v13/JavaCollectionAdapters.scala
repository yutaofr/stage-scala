package clearing.v13

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

object JavaCollectionAdapters:
  def liveView[A](source: java.util.List[A]): mutable.Buffer[A] =
    source.asScala

  def immutableSnapshot[A](source: java.util.List[A]): List[A] =
    source.asScala.toList

  def validTransactionCodes(
    source: java.util.List[String]
  ): List[String] =
    immutableSnapshot(source).flatMap(code =>
      Option(code).filter(_.startsWith("TX-"))
    )

  def balances(
    source: java.util.Map[String, java.lang.Double]
  ): Map[String, BigDecimal] =
    source.asScala.iterator
      .flatMap: (key, value) =>
        for
          safeKey <- Option(key)
          safeValue <- Option(value)
        yield safeKey -> BigDecimal.valueOf(safeValue.doubleValue())
      .toMap

  def toJava[A](source: List[A]): java.util.List[A] =
    source.asJava
