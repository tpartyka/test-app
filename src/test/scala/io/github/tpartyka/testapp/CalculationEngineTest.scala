package io.github.tpartyka.testapp

import org.scalatest.{FlatSpec, Matchers}

import scala.util.Success

/**
  * Created by tpartyka on 28.05.2017.
  */
class CalculationEngineTest extends FlatSpec with Matchers {

  "Expression 2 * (3 + 4)" should "return 14" in {
    val request = "2 * (3 + 4)"
    CalculationEngine.validate(request).map(_.evaluate) should matchPattern { case Success(14) => }
  }

  "Expression 6 * (3+2) + (7 + 5 - 4)" should "be computed in parallel" in {
    val request = "6 * (3+2) + (7 + 5 - 4)"
    CalculationEngine.validate(request).map(_.evaluate) should matchPattern { case Success(38) => }
  }
}
