package io.github.tpartyka.testapp

import akka.actor.Actor
import akka.event.{Logging, LoggingAdapter}
import akka.actor.SupervisorStrategy.Stop
import io.github.tpartyka.testapp.api._
import io.github.tpartyka.testapp.AkkaEnv.system
import scala.util.{Failure, Success, Try}

/**
  * Created by tpartyka on 30.05.2017.
  */
class CalculationActor extends Actor {

  val log = Logging(system, "treeEvaluation")

  override def preStart(): Unit = {
    log.info("Starting calculation actor.")
  }

  override def postStop(): Unit = {
    log.info("Calculation actor stopped.")
  }

  override def receive: Receive = {
    case CalculationRequest(expression) =>
      CalculationEngine.validate(expression) match {
        case Success(tree) => computeTree(tree)
        case Failure(exception) => respondSender(CalculationFailed(s"Input data failure. [${exception.getMessage}]", 400))
      }
  }

  def computeTree(tree: Tree): Unit = {
    Try(tree.evaluate) match {
      case Success(result) => respondSender(CalculationSuccess(result))
      case Failure(exception) => respondSender(CalculationFailed(s"Exception during calculation. [${exception.getMessage}]", 500))
    }
  }

  def respondSender(resp: CalculationResponse): Unit = {
    sender ! resp
    context.stop(self)
  }

}
