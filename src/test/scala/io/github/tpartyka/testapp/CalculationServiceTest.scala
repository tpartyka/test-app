package io.github.tpartyka.testapp

import akka.http.scaladsl.model._
import akka.util.ByteString
import io.github.tpartyka.testapp.api.{CalculationFailed, CalculationSuccess}

class CalculationServiceTest extends ServiceTestBase with CalculationService {

  import io.circe.generic.auto._

  object PostRequest {
    def withBody(request: String): HttpRequest = {
      HttpRequest(
        HttpMethods.POST,
        uri = "/evaluate",
        entity = HttpEntity(MediaTypes.`application/json`, ByteString(
          s"""
             |{
             |    "expression": "$request"
             |}
        """.stripMargin)))
    }
  }

  "CalculationService" when {
    "postRequest contains 0" should {
      "return 0" in {
        PostRequest.withBody("2 + 2") ~> routes ~> check {
          status should be(StatusCodes.OK)
          responseAs[CalculationSuccess].result shouldEqual 4
        }
      }
      "postRequest contains division by 0" should {
        "return error message" in {
          PostRequest.withBody("1/0") ~> routes ~> check {
            status should be(StatusCodes.BadRequest)
            responseAs[CalculationFailed].error should include("isInfinity: true")
          }
        }
      }
      "postRequest with unexpected char" should {
        "return error message" in {
          PostRequest.withBody("1&5") ~> routes ~> check {
            status should be(StatusCodes.BadRequest)
            responseAs[CalculationFailed].error should include("Invalid input:")
          }
        }
      }
    }
  }
}
