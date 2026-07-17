package clearing.v13

import java.util.{ArrayList, HashMap}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters.*

final class JavaCollectionAdaptersSpec extends AnyFlatSpec with Matchers:
  "LegacyJavaMock" should "exposer de vraies collections Java mutables" in:
    val mock = new LegacyJavaMock()

    mock.transactionCodes() shouldBe a[ArrayList[?]]
    mock.transactionCodes().asScala.toList shouldBe List(
      "TX-001",
      "AUDIT-002",
      "TX-003",
      "REJECT-004"
    )
    mock.bankBalances() shouldBe a[HashMap[?, ?]]
    mock.bankBalances().get("ATH") shouldBe 10.25d

  "JavaCollectionAdapters.liveView" should "refléter une mutation Java ultérieure" in:
    val source = new ArrayList[String]()
    source.add("TX-001")
    val view = JavaCollectionAdapters.liveView(source)

    source.add("TX-002")

    view.toList shouldBe List("TX-001", "TX-002")

  "JavaCollectionAdapters.immutableSnapshot" should "isoler la logique Scala des mutations Java" in:
    val source = new ArrayList[String]()
    source.add("TX-001")
    val snapshot = JavaCollectionAdapters.immutableSnapshot(source)

    source.add("TX-002")

    snapshot shouldBe List("TX-001")

  "JavaCollectionAdapters.validTransactionCodes" should "permettre filter et map dans le monde Scala" in:
    val codes = JavaCollectionAdapters.validTransactionCodes(
      new LegacyJavaMock().transactionCodes()
    )

    codes shouldBe List("TX-001", "TX-003")
    codes.map(_.toLowerCase) shouldBe List("tx-001", "tx-003")

  it should "ignorer explicitement un null venu de Java" in:
    val codes = new ArrayList[String]()
    codes.add("TX-001")
    codes.add(null)
    codes.add("AUDIT-002")

    JavaCollectionAdapters.validTransactionCodes(codes) shouldBe
      List("TX-001")

  "JavaCollectionAdapters.balances" should "convertir Double avec une représentation décimale stable" in:
    JavaCollectionAdapters.balances(
      new LegacyJavaMock().bankBalances()
    ) shouldBe Map(
      "ATH" -> BigDecimal("10.25"),
      "CIH" -> BigDecimal("-3.5"),
      "BOA" -> BigDecimal("0.1")
    )

  it should "ignorer les clés et valeurs nulles venues de Java" in:
    val balances = new HashMap[String, java.lang.Double]()
    balances.put("ATH", 10.25d)
    balances.put("CIH", null)
    balances.put(null, 5d)

    JavaCollectionAdapters.balances(balances) shouldBe Map(
      "ATH" -> BigDecimal("10.25")
    )

  "JavaCollectionAdapters.toJava" should "rendre les éléments Scala dans le même ordre" in:
    val javaCodes = JavaCollectionAdapters.toJava(
      List("TX-010", "TX-011")
    )

    javaCodes shouldBe a[java.util.List[?]]
    javaCodes.asScala.toList shouldBe List("TX-010", "TX-011")
