package com.jambit.fpinpractice

import zio.http.{Request, Response, Status}

//noinspection ScalaWeakerAccess
object Examples {

  object TypeParameters {

    def toStringArray(value: String): Array[String] =
      Array(value)

    def toArray[T](value: T): Array[T] =
      Array(value)

  }

  object HigherOrderFunctions {

    def applyFunction[I, O](value: I, fun: I => O): O =
      fun(value)

    // n == 84
    val n = applyFunction(42, (i: Int) => i * 2)

  }

  object Map {

    val mapFunction: Int => Int = i => i * 2

    // List(46, 84)
    val nonEmpty: List[Int] = List(23, 42).map(mapFunction)
    // Nil
    val empty: List[Int] = Nil.map(mapFunction)

    // Some(84)
    val some: Option[Int] = Some(42).map(mapFunction)
    // None
    val none: Option[Int] = None.map(mapFunction)

    // Right(84)
    val right: Either[String, Int] = Right(42).map(mapFunction)
    // Left("Error!")
    val left: Either[String, Int] = Left("Error!").map(mapFunction)

  }

  object FlatMap {

    // List(List(1, 23), List(1, 42))
    val nestedList: List[List[Int]] = List(23, 42).map(i => List(1, i))

    // List(1, 23, 1, 42)
    val flattenedList: List[Int] = nestedList.flatten

    // List(1, 23, 1, 42)
    val flattenedList2: List[Int] = List(23, 42).flatMap(i => List(1, i))

    def safeDivisionWithOption(a: Int, b: Int): Option[Int] =
      Some(b)
        .flatMap(b =>
          if (b == 0) None
          else Some(b)
        )
        .map(b => a / b)

    def safeDivisionWithEither(a: Int, b: Int): Either[String, Int] =
      Right(b)
        .flatMap(b =>
          if (b == 0) Left("Division by zero is not possible!")
          else Right(b)
        )
        .map(b => a / b)

  }

  //noinspection SimplifiableFoldOrReduce,ScalaUnusedSymbol
  object Fold {

    val list = List(1, 2, 3, 4)
    // sum == 10
    val sum = list.fold(0)((sum, current) => sum + current)
    // string == "0123"
    val string = list.fold("")((string, current) => s"$string$current")
    // string2 == "3210"
    val string2 = list.foldRight("")((string, current) => s"$string$current")

    val either: Either[String, Int] = Right(42)

    val eitherValue = either.fold(left => Nil, right => List(right))

    val option = None
    // x == 0
    val optionValue = option.getOrElse(0)
  }

  object PatternMatching {

    val either: Either[String, Int] = Right(42)

    val foldedValue = either.fold(
      left => Nil,
      right => List(right)
    )

    val matchedValue = either match {
      case Left(left) => Nil
      case Right(right) => List(right)
    }

  }

  object PatternMatching2 {

    sealed trait MyResult

    case class MySuccess(body: String) extends MyResult

    case class MyFailure(status: Int, message: String) extends MyResult

    def toResponse(backendResult: MyResult): Response =
      backendResult match {
        case MySuccess(body) => Response.text(body)
        case MyFailure(500, _) => internalServerError
        case MyFailure(code, msg) =>
          Status.fromInt(code)
            .map(status => Response.text(msg).withStatus(status))
            .getOrElse(internalServerError)
      }

    val internalServerError = Response
      .text("Internal Server Error")
      .withStatus(Status.InternalServerError)

    def email(userId: String): Option[String] = ???

    def hasAccess(email: String): Option[Boolean] = ???

    def fullName(email: String): Option[String] = ???
  }

  object ForComprehension {
    case class Error(status: Status)

    case class RequestBody(email: String)

    case class UserData()

    case class ResponseBody(email: String, userData: UserData)

    case class AuthData(authorized: Boolean, userId: String)

    val userId = "1234"

    def validateBody(request: Request): Either[Error, RequestBody] = ???
    def checkAuthorization(request: Request): Either[Error, AuthData] = ???
    def userData(userId: String): Either[Error, UserData] = ???

    def handleRequestWithClosure(request: Request): Either[Error, ResponseBody] =
      validateBody(request)
        .flatMap(body => checkAuthorization(request)
          .flatMap(authData =>
            if (authData.authorized) Right(body)
              .flatMap(body => userData(body.email))
              .map(userData => ResponseBody(authData.userId, userData))
            else Left(Error(Status.Forbidden))
          )
        )

    def handleRequestWithPipe(request: Request): Either[Error, ResponseBody] =
      validateBody(request)
        .flatMap(body => checkAuthorization(request)
          .map(authorized => Tuple2(body, authorized))
        )
        .flatMap(bodyAuthData =>
          if (bodyAuthData._2.authorized) Right(bodyAuthData)
          else Left(Error(Status.Forbidden))
        )
        .flatMap(bodyAuthorized => userData(bodyAuthorized._2.userId)
          .map(userData => ResponseBody(bodyAuthorized._2.userId, userData))
        )

    def handleRequestWithForComprehension(request: Request): Either[Error, ResponseBody] =
      for {
        body <- validateBody(request)
        authData <- checkAuthorization(request)
        _ <- if (authData.authorized) Right(body) else Left(Error(Status.Forbidden))
        userData <- userData(body.email)
      } yield ResponseBody(authData.userId, userData)
  }

}
