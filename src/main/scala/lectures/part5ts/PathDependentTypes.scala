package lectures.part5ts

object PathDependentTypes extends App{

  class Outer {
    class Inner
    object InnerObject
    type InnerType

    def print(i: Inner): Unit = println(i)
    def printGeneral(i: Outer#Inner): Unit = println(i)
  }

  def aMethod: Int = {
    class HelperClass
    type HelperType = String
    2
  }

  // per-instance
  val outer = new Outer
  val inner = new outer.Inner // outer.Inner is a TYPE

  val oo = new Outer
  // val otherInner: oo.Inner = new outer.Inner

  outer.print(inner)
  // oo.print(inner)

  // path-dependent types

  // Outer#Inner
  outer.printGeneral(inner)
  oo.printGeneral(inner)

  /*
    Exercise
    Note: this exercise is for Scala 2 only.
    DB keyed by Int or String, but maybe others
  */
  /*
  Hint:
  - use path-dependent types
  - abstract type members and/or type aliases
  */

  trait ItemLike {
    type Key
  }

  trait Item[K] extends ItemLike {
    type Key = K
  }
  trait IntItem extends Item[Int]
  trait StringItem extends Item[String]

  // def get[ItemType <: ItemLike](key: ItemType#Key): ItemType = ???

  // get[IntItem](42) // ok
  // get[StringItem]("home") // ok

  // get[IntItem]("Scala") // not ok
}
