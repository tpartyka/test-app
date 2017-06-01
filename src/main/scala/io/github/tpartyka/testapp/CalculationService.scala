package io.github.tpartyka.testapp

import akka.actor.Props
import akka.http.scaladsl.server.{Directives, Route, StandardRoute}
import akka.pattern.ask
import akka.util.Timeout
import io.github.tpartyka.testapp.AkkaEnv._
import io.github.tpartyka.testapp.api.CalculationRequest

import scala.concurrent.duration._

trait CalculationService extends BaseService {

  import Directives._
  import io.circe.generic.auto._

  implicit val timeout: Timeout = 3.seconds

  override protected def routes: Route =
    path("evaluate") {
      post {
        entity(as[CalculationRequest]) { request: CalculationRequest =>
          onComplete(
            system.actorOf(Props[CalculationActor]).ask(request).mapTo[StandardRoute]
          ) {
            response => response.fold(failWith, route => route)
          }
        }
      }
    }
}
