package io.github.tpartyka.testapp.api

/**
  * Created by tpartyka on 24.05.2017.
  */

sealed class CalculationResponse

case class CalculationSuccess(result: Float) extends CalculationResponse

case class CalculationFailed(reason: String, returnCode: Int) extends CalculationResponse

