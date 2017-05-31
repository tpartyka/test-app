package io.github.tpartyka.testapp


import akka.event.Logging
import io.github.tpartyka.testapp.AkkaEnv.system

import scala.util.Try
import scala.util.parsing.combinator.JavaTokenParsers

/**
  * Created by tpartyka on 25.05.2017.
  */
sealed class CalculationEngine extends JavaTokenParsers {

  type FunctionMapping = PartialFunction[String, (Float, Float) => Float]

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

  private lazy val numeric = floatingPointNumber ^^ { t => Leaf(t.toFloat) }

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

  def evaluate: Float = evaluate(this)

  private def evaluate(root: Tree): Float = root match {
    case Leaf(value) =>
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

case class Node(operation: (Float, Float) => Float, children: Tree*) extends Tree

case class Leaf(value: Float) extends Tree