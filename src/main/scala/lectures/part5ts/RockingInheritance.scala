package lectures.part5ts

object RockingInheritance extends App {

  // convenience
  trait Writer[T] {
    def write(value: T): Unit
  }
  trait Closeable {
    def close(status: Int): Unit
  }
  trait GenericStream[T] {
    // some methods
    def foreach(f: T => Unit): Unit
  }

  def processStream[T](stream: GenericStream[T] with Writer[T] with Closeable): Unit = {
    stream.foreach(println)
    stream.close(0)
  }

  // diamond problem

  trait Animal { def name: String }
  trait Lion extends Animal { override def name: String = "Lion" }
  trait Tiger extends Animal { override def name: String = "Tiger" }
  class Liger extends Lion with Tiger

  val mike = new Liger
  println(mike.name) // prints Tiger

  /*
    Liger extends Animal with { override def name: String = "Lion" }
    with { override def name: String = "Tiger" }

    LAST OVERRIDE GETS PICKED
  */

  // super problem + type linearization

  trait Cold {
    def print: Unit = println("cold")
  }

  trait Green extends Cold {
    override def print: Unit = {
      println("green")
      super.print
    }
  }

  trait Blue extends Cold {
    override def print: Unit = {
      println("blue")
      super.print
    }
  }

  class Red {
    def print: Unit =  println("red")
  }

  class White extends Red with Green with Blue {
    override def print: Unit = {
      println("white")
      super.print
    }
  }

  val color = new White
  color.print // prints white, blue, green, cold

  /*
  WHO'S MY SUPER?
  Cold = AnyRef with <Cold>
  Green
        = Cold with <Green>
        = AnyRef with <Cold> with <Green>
  Blue
        = Cold with <Blue>
        = AnyRef with <Cold> with <Blue>

  Red = AnyRef with <Red>

  White = Red with Green with Blue with <White>
        = AnyRef with <Red>
          with (AnyRef with <Cold> with <Green>)
          with (AnyRef with <Cold> with <Blue>)
          with <White>

        = AnyRef with <Red> with <Cold> with <Green> with <Blue> with <White>
          ^^^^^^^^^^^^^^^^^<= Type linearization =>^^^^^^^^^^^^^^^^^^^^^^^^^^

  = prints white calls super(blue)
  prints blue calls super(green)
  prints green calls super(cold)
  prints cold
  = white
  blue
  green
  cold
  */

}
