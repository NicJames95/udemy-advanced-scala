package lectures.part4implicits

object OrganizingImplicits extends App {

  implicit val reverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  // implicit val normalOrdering: Ordering[Int] = Ordering.fromLessThan(_ < _)
  println(List(1,4,5,3,2).sorted)

  // scala.Predef

  /*
    Implicits (used as implicit parameters):
      - val/var
      - object
      - accessor methods = defs with no parentheses
  */

  // Exercise
  case class Person(name: String, age: Int)


  val persons = List(
    Person("Steve", 30),
    Person("Amy", 22),
    Person("John", 66)
  )

  /*
   Implicit scope
     - normal scope = LOCAL SCOPE
     - imported scope
     - companion objects of all types involved in the method signature
       - List
       - Ordering (companion object)
       - all the types involved = A or any supertype
 */
  // def sorted[B >: A](implicit ord: Ordering[B]): List[B]

  /* BEST PRACTICES
  - When defining an implicit val:
  #1
  - if there is a SINGLE POSSIBLE value for it
  - and you can edit the code for the type
  then define the implicit in the companion object

  #2
  - if there are many possible values for it
  - but a single GOOD one
  - and you can edit the code for the type
  then define the GOOD implicit in the companion object
  */

//  object Person {
//    implicit val aplphabeticOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
//  }
//  implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.age < b.age)
//  println(persons.sorted)
  object AlphabeticNameOrdering {
    implicit val alphabeticOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }
  object AgeOrdering {
    implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.age < b.age)
  }

  import AlphabeticNameOrdering._
  println(persons.sorted)

  /*
    Exercise.

    - totalPrice = most used (50%)
    - by unit count = (25%)
    - by unit price = (25%)
  */
  case class Purchase(nUnits: Int, unitPrice: Double)

  object Purchase {
    implicit val totalPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan((a, b) => a.nUnits * a.unitPrice < b.nUnits * b.unitPrice)
  }

  object UnitCountOrdering {
    implicit val unitCountOrdering: Ordering[Purchase] = Ordering.fromLessThan((a, b) => a.nUnits < b.nUnits)
  }
  object UnitPriceOrdering {
    implicit val unitPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan((a, b) => a.unitPrice < b.unitPrice)
  }



}
