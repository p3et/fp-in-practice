
package com.jambit.fpinpractice

import zio._
import zio.http._

object MainApp extends ZIOAppDefault {
  private val httpApps = GreetingApp()

  def run: ZIO[Environment with ZIOAppArgs with Scope, Throwable, Any] = {
    val port = 8080

    Server
      .serve(
        httpApps.withDefaultErrorResponse
      )
      .provide(
        Server.defaultWithPort(port).tap(_ => ZIO.logInfo(s"Greeting server running at port $port"))
      )
  }
}

object GreetingApp {
  def apply(): Http[Any, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      // GET /
      case Method.GET -> Root => ZIO.succeed(Response.text(s"Hello World!"))

      // GET /greet/:name
      case Method.GET -> Root / "greet" / name => handleErrors(greeting(name))
    }

  private case class MyError(status: Status, message: String)

  private def handleErrors(effect: ZIO[Any, MyError, String]): ZIO[Any, Nothing, Response] =
    effect
      .map(body => Response.text(body))
      .catchAll(error => ZIO.succeed(error match {
        // catch 500
        case MyError(Status.InternalServerError, message) =>
          scala.Console.println(message) // log message
          Response.status(Status.InternalServerError) // don't reveal insides
        case MyError(status, message) => Response.text(message).withStatus(status)
      }))

  private def greeting(name: String): ZIO[Any, MyError, String] =
    for {
      _ <-
        if ("[0-9]".r.findFirstIn(name).isEmpty) ZIO.succeed(name)
        else ZIO.fail(MyError(Status.BadRequest, "Name must not contain numbers!"))
      lowerCase <-
        ZIO.succeed(name.toLowerCase())
      _ <-
        if (lowerCase != "adolf") ZIO.succeed(name)
        else ZIO.fail(MyError(Status.Forbidden, "We don't greet Adolf!"))
      upperCase <-
        ZIO.attempt({
          if (name.length < 3) throw new RuntimeException("Developer did not like short names!")
          else name.toUpperCase
        }).mapError(ex => MyError(Status.InternalServerError, ex.getMessage))
      alternatingCase <-
        alternateCase(lowerCase, upperCase)
    } yield s"Hello $alternatingCase!"

  private def alternateCase(lowerCase: String, upperCase: String): ZIO[Any, MyError, String] = {
    ZIO.attempt({
      upperCase
        .zipWithIndex
        .map(charIndex =>
          if (charIndex._2 % 2 == 0) charIndex._1
          else lowerCase.charAt(charIndex._2)
        )
        .foldLeft("")((concatenation, char) => concatenation + char)
    }
    ).mapError(ex => MyError(Status.InternalServerError, ex.getMessage))
  }
}