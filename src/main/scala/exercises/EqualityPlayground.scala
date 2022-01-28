package exercises

import lectures.part4implicits.TypeClasses.{User, UserSerializer}

object EqualityPlayground extends App {

  /**
   * Equality
   */
  trait Equal[T] {
    def compare(value: T, other: T): Boolean
  }

  object IntegerEqual extends Equal[Ints] {
    def compare(value: Ints, other: Ints): Boolean = if value == other then true else false
  }

  implicit object NameEquality extends Equal[User] {
    def compare(a: User, b: User): Boolean = a.name == b.name
  }

  object FullEquality extends Equal[User] {
    def compare(a: User, b: User): Boolean = a.name == b.name && a.email == b.email
  }

  case class Ints(value: Int)
  case class Doubles(value: Double)

  val int = Ints(24)
  val twentyThree = Ints(23)
  /// println(IntegerEqual.compare(int, twentyThree))


  /*
    Exercise: implement the Type Class pattern for the Equality type class.
  */

  object Equal {
    def apply[T](a: T, b: T)(implicit equalizer: Equal[T]): Boolean =
      equalizer.compare(a, b)
  }

  val julius = User("Julius", 18, "julius@rockthejvm.com")
  println(UserSerializer.serialize(julius))

  val anotherJohn = User("John", 45, "anotherJohn@rockthejvm.com")
  println(Equal.apply(julius, anotherJohn))
  // AD-HOC polymorphism

  /*
    Exercise - improve the Equal TC with an implicit conversion class
    ===(anotherValue: T)
    !==(anotherValue: T)
  */

  implicit class TypeSafeEqual[T](value: T) {
    def ===(other: T)(implicit equalizer: Equal[T]): Boolean = equalizer.compare(value, other)
    def !==(other: T)(implicit equalizer: Equal[T]): Boolean = ! equalizer.compare(value, other)
  }

  println(julius === anotherJohn)
  /*
    julius.===(anotherJohn)
    new TypeSafeEqual[User](julius).===(anotherJohn)
    new TypeSafeEqual[User](julius).===(anotherJohn)(NameEquality)
  */
  println(julius !== anotherJohn)
  /*
    TYPE SAFE
    - Scala 3 added a feature called "multiversal equality", and julius == 42 will not compile
      println(julius == 42)
      println(julius === 43) // TYPE SAFE must be same type
  */

}
