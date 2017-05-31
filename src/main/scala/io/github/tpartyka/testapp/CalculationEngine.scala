package io.github.tpartyka.testapp


import scala.util.Try
import scala.util.parsing.combinator.JavaTokenParsers
import io.github.tpartyka.testapp.AkkaEnv.system
import akka.event.{Logging, LoggingAdapter}

/**
  * Created by tpartyka on 25.05.2017.
  */
sealed class CalculationEngine extends JavaTokenParsers {

  type FunctionMapping = PartialFunction[String, (Double, Double) => Double]

  val operationMapping: FunctionMapping = {
    case "+" => (x, y) => x + y
    case "-" => (x, y) => x - y
    case "*" => (x, y) => x * y
    case "/" => (x, y) => x / y
    case _ => throw new UnsupportedOperationException
  }

  def parseTree(stringExpression: String): Tree = {
    parseAll(expr, stringExpression).getOrElse(throw new IllegalArgumentException)
  }
  private lazy val factor = "(" ~> expr <~ ")" | numeric

  private lazy val numeric = floatingPointNumber ^^ { t => Leaf(t.toDouble) }

  private lazy val expr: Parser[Tree] = term ~ rep("[+-]".r ~ term) ^^ {
    case t ~ ts => ts.foldLeft(t) {
      case (t1, op ~ t2) => Node(operationMapping(op), t1, t2)
    }
  }

  private lazy val term: Parser[Tree] = factor ~ rep("[*/]".r ~ factor) ^^ {
    case t ~ ts => ts.foldLeft(t) {
      case (t1, op ~ t2) => Node(operationMapping(op), t1, t2)
    }
  }

}

object CalculationEngine {

  def validate(request: String): Try[Tree] = Try {
    new CalculationEngine().parseTree(request)
  }

}

sealed abstract class Tree {

  val log = Logging(system, "treeEvaluation")

  def evaluate: Double = evaluate(this)

  private def evaluate(root: Tree): Double = root match {
    case Leaf(value: Double) =>
      log.debug(s"Returning $value")
      value
    case Node(op, leafs@_*) if leafs.forall(_.isInstanceOf[Leaf]) =>
      log.debug(s"Computing $op, args: $leafs\n")
      leafs.map(evaluate).reduce(op)
    case Node(op, nodes@_*) =>
      log.debug(s"Computing $op, args: $nodes\n")
      nodes.par.map(evaluate).reduce(op)
  }

}

case class Node(operation: (Double, Double) => Double, children: Tree*) extends Tree

case class Leaf(value: Double) extends Tree