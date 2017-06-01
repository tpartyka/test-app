package io.github.tpartyka.testapp

import akka.actor.Actor
import akka.event.Logging
import akka.http.scaladsl.server._
import akka.http.scaladsl.model.StatusCodes._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.github.tpartyka.testapp.AkkaEnv.system
import io.github.tpartyka.testapp.api._

import scala.util.Try

/**
  * Created by tpartyka on 30.05.2017.
  */
class CalculationActor extends Actor with FailFastCirceSupport {

  import Directives._
  import io.circe.generic.auto._

  val log = Logging(system, "calculationActor")

  override def preStart(): Unit = {
    log.info("Starting calculation actor.")
  }

  override def postStop(): Unit = {
    log.info("Calculation actor stopped.")
  }

  override def receive: Receive = {
    case CalculationRequest(expression) =>
      log.info(s"Start processing $expression.")
      CalculationEngine.validate(expression).fold(
        exception => sender ! complete(BadRequest, CalculationFailed(exception.getMessage)),
        computeTree
      )
      context.stop(self)
  }

  def computeTree(tree: Tree): Unit = {
    Try(tree.evaluate).fold(
      exception => sender ! complete(BadRequest, CalculationFailed(exception.getMessage)),
      result => sender ! complete(CalculationSuccess(result))
    )
  }


}
