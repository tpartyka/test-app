package io.github.tpartyka.testapp

import akka.http.scaladsl.server.{Directives, Route}
import io.github.tpartyka.testapp.api.{CalculationRequest, CalculationResponse}

trait CalculationService extends BaseService {

  import Directives._
  import io.circe.generic.auto._

  protected case class Status(uptime: String)

  override protected def routes: Route =
    path("evaluate") {
      post {
        entity(as[CalculationRequest]) { case request@CalculationRequest(str) =>
          complete(CalculationResponse(0))
        }
      }
    }
}
