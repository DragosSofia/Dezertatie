package example;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.*;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class UserWorkflowSimulation extends Simulation {

  HttpProtocolBuilder httpProtocol =
      http.baseUrl("http://localhost:8081")
          .acceptHeader("*/*")
          .contentTypeHeader("application/json")
          .userAgentHeader("PostmanRuntime/7.51.0");

  // Login JSON template
  String loginBodyTemplate =
      """
                    {
                        "username": "${username}",
                        "password": "${password}"
                    }
                    """;

  String getDataBody =
      """
                   {
                    "startDate": "2026-05-16T01:30:00",
                    "endDate": "2026-05-19T14:30:00"
                    }
                   """;

  String registerAccountBody =
      """
                    {
                        "email": "${username}",
                        "emailUpb": "${username}",
                        "password": "${password}"
                    }
                    """;

  FeederBuilder.Batchable<String> userFeeder = csv("users.csv").circular();

  ScenarioBuilder scn =
      scenario("User register")
          .feed(userFeeder)
          //          .exec(
          //              http("Register Account")
          //                  .post("/auth/register")
          //                  .body(
          //                      StringBody(
          //                          session ->
          //                              """
          //                                                                    {
          //                                                                        "email": "%s",
          //                                                                        "emailUpb":
          // "%s",
          //                                                                        "password": "%s"
          //                                                                    }
          //                                                                    """
          //                                  .formatted(
          //                                      session.getString("username"),
          //                                      session.getString("username"),
          //                                      session.getString("password")))))
          .exec(
              session -> {
                System.out.println(
                    "username="
                        + (session.contains("username")
                            ? session.getString("username")
                            : "<missing>"));
                System.out.println(
                    "password="
                        + (session.contains("password")
                            ? session.getString("password")
                            : "<missing>"));
                System.out.println("passwordLen=" + session.getString("password").length());
                return session;
              })
          .exec(
              http("Login Request")
                  .post("/auth/login")
                  .body(
                      StringBody(
                          session ->
                              """
                                                                    {
                                                                      "username": "%s",
                                                                      "password": "%s"
                                                                    }
                                                                    """
                                  .formatted(
                                      session.getString("username"),
                                      session.getString("password"))))
                  .asJson()
                  .check(status().saveAs("loginStatus"))
                  .check(bodyString().saveAs("loginResponseBody"))
                  .check(jsonPath("$.token").optional().saveAs("authToken")))
          .exec(
              session -> {
                System.out.println(
                    "Login status="
                        + (session.contains("loginStatus")
                            ? session.getInt("loginStatus")
                            : "<missing>"));
                System.out.println(
                    "Login response body="
                        + (session.contains("loginResponseBody")
                            ? session.getString("loginResponseBody")
                            : "<missing>"));
                System.out.println(
                    "authToken="
                        + (session.contains("authToken")
                            ? session.getString("authToken")
                            : "<missing>"));
                return session;
              })
          .exec(
              http("Get Measurements")
                  .get("/resource/measurements")
                  .header("jwtCookie", "#{authToken}")
                  .check(status().is(200)))
          .exec(
              repeat(25)
                  .on(
                      exec(
                          http("Get Data")
                              .get(
                                  "/resource/measurements/weather/data?fields=temperature,humidity")
                              .header("jwtCookie", "#{authToken}")
                              .body(StringBody(getDataBody))
                              .check(status().is(200)))));

  {
    setUp(scn.injectOpen(rampUsers(800).during(30))).protocols(httpProtocol);
  }
}
