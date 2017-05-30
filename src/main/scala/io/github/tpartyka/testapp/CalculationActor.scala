package io.github.tpartyka.testapp

import akka.actor.Actor
import akka.actor.SupervisorStrategy.Stop
import io.github.tpartyka.testapp.api.{CalculationFailed, CalculationRequest, CalculationResponse, CalculationSuccess}

import scala.util.{Failure, Success, Try}

/**
  * Created by tpartyka on 30.05.2017.
  */
class CalculationActor extends Actor {
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
    self ! Stop
  }

}
