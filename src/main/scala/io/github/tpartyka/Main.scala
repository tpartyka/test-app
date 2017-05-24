package io.github.tpartyka

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.{CirceSupport, FailFastCirceSupport}

import scala.concurrent.ExecutionContext

trait BaseComponent extends Config {
  protected implicit def log: LoggingAdapter
  protected implicit def executor: ExecutionContext
}

trait BaseService extends BaseComponent with FailFastCirceSupport {
  protected def routes: Route
}

object Main extends App with Config with CalculationService {
  implicit val system       = ActorSystem()
  implicit val materializer = ActorMaterializer()

  override protected def log      = Logging(system, "service")
  override protected def executor = system.dispatcher

  Http().bindAndHandle(routes, httpConfig.interface, httpConfig.port)
}

