package io.github.tpartyka.testapp


import akka.event.Logging
import io.github.tpartyka.testapp.AkkaEnv.system

import scala.util.Try
import scala.util.parsing.combinator.JavaTokenParsers

/**
  * Created by tpartyka on 25.05.2017.
  */
object CalculationEngine extends JavaTokenParsers {

  type FunctionMapping = PartialFunction[String, (Double, Double) => Double]

  val operationMapping: FunctionMapping = {
    case "+" => (x, y) => x + y
    case "-" => (x, y) => x - y
    case "*" => (x, y) => x * y
    case "/" => (x, y) => x / y
    case _ => throw new UnsupportedOperationException
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

  def validate(request: String): Try[Tree] = Try {
    parseAll(expr, request).getOrElse(throw new IllegalArgumentException)
  }

}

sealed abstract class Tree {

  val log = Logging(system, "treeEvaluation")

  def evaluate: Double = {
    val result = evaluate(this)
    if (result.isInfinity || result.isInfinity) {
      throw new ArithmeticException(s"Result isNaN: ${result.isNaN}, isInfinity: ${result.isInfinity}")
    }
    result
  }

  private def evaluate(root: Tree): Double = root match {
    case Leaf(value) =>
      log.debug(s"Returning $value")
      value
    case Node(op, leafs@_*) if leafs.forall(_.isInstanceOf[Leaf]) =>
      log.debug(s"Computing leafs-only node, args: $leafs\n")
      leafs.map(evaluate).reduce(op)
    case Node(op, nodes@_*) =>
      log.debug(s"Computing node, args: $nodes\n")
      nodes.par.map(evaluate).reduce(op)
  }

}

case class Node(operation: (Double, Double) => Double, children: Tree*) extends Tree

case class Leaf(value: Double) extends Tree