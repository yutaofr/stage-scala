import io.gatling.core.Predef.*
import io.gatling.http.Predef.*
import scala.concurrent.duration.*

class ClearingLoadSimulation extends Simulation:
  private val httpProtocol = http.baseUrl("http://localhost:8080")

  private val feeder = Iterator.from(1).map { n =>
    Map("txId" -> s"load-$n", "amount" -> ((n % 1000) + 1).toString)
  }

  private val scenarioUnderTest =
    scenario("clearing-load")
      .feed(feeder)
      .exec(
        http("publish-transaction")
          .post("/api/v1/transactions")
          .body(StringBody(
            """{"id":"#{txId}","sender":"AWB","receiver":"CIH","amount":#{amount},"status":"Pending","transactionType":"Transfer"}"""
          ))
          .asJson
          .check(status.is(202))
      )

  setUp(
    scenarioUnderTest.inject(
      rampUsersPerSec(10).to(100).during(60.seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.failedRequests.percent.lt(1.0),
      global.responseTime.percentile(95).lt(500)
    )
