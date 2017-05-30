package io.github.tpartyka.testapp

import akka.http.scaladsl.server.{Directives, Route}
import AkkaEnv._
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import io.github.tpartyka.testapp.api.{CalculationFailed, CalculationRequest, CalculationResponse, CalculationSuccess}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait CalculationService extends BaseService {

  import Directives._
  import io.circe.generic.auto._

  implicit val timeout: Timeout = 3.seconds

  override protected def routes: Route =
    path("evaluate") {
      post {
        entity(as[CalculationRequest]) { request: CalculationRequest =>
            onComplete(system.actorOf(Props[CalculationActor]).ask(request).mapTo[CalculationResponse]) {
              case Success(response)=> response match {
                case c: CalculationFailed => complete(c.returnCode, c.reason)
                case s: CalculationSuccess => complete(s)
              }
              case Failure(ex) => complete(500, ex.getMessage)
            }
        }
      }
    }
}
