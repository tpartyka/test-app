package io.github.tpartyka.testapp.api

/**
  * Created by tpartyka on 24.05.2017.
  */

sealed trait CalculationResponse

case class CalculationSuccess(result: Double) extends CalculationResponse

case class CalculationFailed(error: String) extends CalculationResponse

