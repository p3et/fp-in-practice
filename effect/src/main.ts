import {Effect, pipe} from "effect"
import {Api, NodeServer, RouterBuilder} from "effect-http"
import {PrettyLogger} from "effect-log"
import * as Schema from "@effect/schema/Schema"
import {internalServerError, make, ServerError} from "effect-http/ServerError"

const api = pipe(
  Api.api({title: "Greeting API"}),
  // GET /
  Api.get(
    "root",
    "/",
    {response: Schema.string}
  ),
  // GET /greet/:name
  Api.get(
    "greeting",
    "/greet/:name",
    {
      request: {
        params: Schema.struct({name: Schema.string})
      },
      response: Schema.string
    })
)

const app = RouterBuilder.make(api).pipe(
  RouterBuilder.handle("root", () =>
    Effect.succeed("Hello World!"),
  ),
  RouterBuilder.handle("greeting", ({params}) =>
    handleErrors(greeting(params.name)),
  ),
  RouterBuilder.build,
)

const program = app.pipe(
  NodeServer.listen({port: 3000}),
  Effect.provide(PrettyLogger.layer()),
)

Effect.runPromise(program)

type MyError = { readonly status: number, readonly message: string }

function handleErrors(effect: Effect.Effect<never, MyError, string>) : Effect.Effect<never, ServerError, string> {
  return effect.pipe(
    Effect.mapError((error) => {
      if (error.status == 500) {
        console.error(error.message)
        return internalServerError("Internal Server Error")
      }
      return make(error.status, error.message)
    })
  )
}

function greeting(name: string): Effect.Effect<never, MyError, string> {
  return pipe(
    <Effect.Effect<never, MyError, string>>(
      /[0-9]/.test(name)
        ? Effect.fail({status: 400, message: "Name must not contain numbers!"})
        : Effect.succeed(name)),
    Effect.map(
      (name) => name.toLowerCase()
    ),
    Effect.flatMap(
      (lowercase) => lowercase != "adolf"
        ? Effect.succeed(lowercase)
        : Effect.fail({status: 404, message: "We don't greet Adolf!"})
    ),
    Effect.flatMap((lowerCase) =>
      Effect.try({
        try: () => {
          if (name.length < 3) throw Error("Developer did not like short names!")
          return [lowerCase, name.toUpperCase()]
        },
        catch: (error) => <MyError>{status: 500, message: `${error}`}
      })
    ),
    Effect.flatMap(([lowerCase, upperCase]) => alternateCase(lowerCase, upperCase)),
    Effect.map((alternatingCase) => `Hello ${alternatingCase}!`)
  )
}

function alternateCase(lowerCase: string, upperCase: string): Effect.Effect<never, MyError, string> {
  return Effect.try(
    {
      try: () => Array
        .from(upperCase)
        .map((char, index) => index % 2 == 0
          ? char
          : lowerCase.charAt(index))
        .reduce((concatenation, char) => concatenation + char, "")
      ,
      catch: (error) => <MyError>{status: 500, message: `${error}`}
    }
  )
}
