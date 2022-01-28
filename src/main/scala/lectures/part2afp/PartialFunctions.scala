package lectures.part2afp

object PartialFunctions extends App {

  val aFunction = (x: Int) => x + 1 // Function1[Int, Int] === Int => Int

  val aFussyFunction = (x: Int) =>
    if (x == 1) 42
    else if (x == 2) 56
    else if (x == 5) 999
    else throw new FunctionNotApplicableException

  class FunctionNotApplicableException extends RuntimeException

  val aNicerFussyFunction = (x: Int) => x match {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  }
  // {1,2,5} => Int

  val aPartialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  } // partial function value

  println(aPartialFunction(2))
  // println(aPartialFunction(57273))

  // PF utilities
  println(aPartialFunction.isDefinedAt(67))

  // lift
  val lifted = aPartialFunction.lift // Int => Option[Int]

  println(lifted(2))
  println(lifted(98))

  val pfChain = aPartialFunction.orElse[Int, Int] {
    case 45 => 67
  }

  println(pfChain(2))
  println(pfChain(45))

  // PF extend normal functions

  val aTotalFunction: Int => Int = {
    case 1 => 99
  }

  // HOFs accept partial functions as well
  val aMappedList = List(1,2,3).map {
    case 1 => 42
    case 2 => 78
    case 3 => 1000
  }
  println(aMappedList)

  /*
    Note: PF can only have ONE parameter type
  */

  /**
   * Exercise
   * 1 - construct a PF instance yourself (anonymous class)
   * 2 - dumb chatbot as a PF
   *
   */
  val aManualFussyFunction = new PartialFunction[Int, Int] {
    override def apply(v1: Int): Int = v1 match {
      case 1 => 42
      case 2 => 65
      case 5 => 999
    }

    override def isDefinedAt(x: Int): Boolean =
      x == 1 || x == 2 || x == 5
  }

  val chatbot: PartialFunction[String, String] = {
    case "hello" => "Hi, my name is HAL9000"
    case "goodbye" => "Once you start talking to me, there is no return, human!"
    case "call mom" => "Unable to find your phone without your credit card"
    case "self destruct" => "Goodbye cruel world"
    case _ => "Unintelligible"
  }
  scala.io.Source.stdin.getLines().foreach(line => println("chatbot says: " + chatbot(line)))
  // scala.io.Source.stdin.getLines().map(chatbot).foreach(println)

  /* My Example
  val factor6To10: PartialFunction[Int, Int] = {
    case 1 => 6
    case 2 => 12
    case 3 => 18
    case _ => 60
  }
  println(factor6To10(2))
  */

  /**
   * trait PartialFunction[-A, +B] extends (A => B) {
   *  def apply(x: A): B
   *  def isDefinedAt(x: A): Boolean
   * }
   * How to use:  (done with pattern matching!)
   * val simplePF: PartialFunction[Int, Int] = {
   *  case 1 => 42
   *  case 2 => 65
   *  case 3 => 999
   * }
   *
   * Utilities:             Used on other types:
   * isDefinedAt            - map, collect on collections
   * lift                   - recover
   * orElse
   */





}
