package io.github.tpartyka.testapp

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.MalformedRequestContentRejection
import akka.util.ByteString
import io.github.tpartyka.testapp.api.CalculationResponse

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
             |    "expression": $request
             |}
        """.stripMargin)))
    }
  }

  "CalculationService" when {
    "postRequest contains 0" should {
      "return 0" in {
        PostRequest.withBody("\"0\"") ~> routes ~> check {
          status should be(StatusCodes.OK)
          responseAs[CalculationResponse].result shouldEqual 0
        }
      }
    }
    "postRequest is not JSON string" should {
      "return error message" in {
        PostRequest.withBody("1") ~> routes ~> check {
          rejection should matchPattern { case MalformedRequestContentRejection(_, _) => }
        }
      }
    }
  }

}
