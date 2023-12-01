function f() {

  function toStringArray(value: String): String[] {
    return [value]
  }

  function toArray<T>(value: T): T[] {
    return [value]
  }

  toStringArray("")
  toArray("")

  function applyFunction<I, O>(value: I, fun: (i: I) => O): O {
    return fun(value)
  }

  // n == 84
  const n = applyFunction(42, (i) => i * 2)

}