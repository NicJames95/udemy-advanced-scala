package lectures.part5ts

object Variance extends App {

  trait Animal
  class Dog extends Animal
  class Cat extends Animal
  class Crocodile extends Animal

  // what is variance?
  // "inheritance" - type substitution of generics

  class Cage[T]
  // should a cage cat also inherit from cage animal? yes - covariance
  class CCage[+T]
  val ccage: CCage[Animal] = new CCage[Cat]

  // no - invariance
  class ICage[T]
  // val icage: ICage[Animal] = new ICage[Cat]

  // hell no - opposite = contravariance
  class XCage[-T]
  val xcage: XCage[Cat] = new XCage[Animal]

  class InvariantCage[T](val animal: T) // invariant

  // covariant positions
  class CovariantCage[+T](val animal: T) // COVARIANT POSITION

  // class ContravariantCage[-T](val animal: T)
  /*
    val catCage: XCage[Cat] = new XCage[Animal](new Crocodile)
  */

  // class CovariantVariableCage[+T](var animal: T) // types of vars are in CONTRAVARIANT POSITION
  /*
    val ccage: CCage[Animal] = new CCage[Cat](new Cat)
    ccage.animal = new Crocodile
  */
  // class ContravariantVariableCage[-T](var animal: T) // also in COVARIANT POSITION
  /*
    val catCage: XCage[Cat] = new XCage[Animal](new Crocodile)
  */
  class InvariantVariableCage[T](var animal: T) // ok

  // trait AnotherCovariantCage[+T] {
  //  def addAnimal(animal: T) // CONTRAVARIANT POSITION
  // }
  /*
    val ccage: CCage[Animal] = new CCage[Dog]
    ccage.add(new Cat)
  */

  class AnotherContravariantCage[-T] {
    def addAnimal(animal: T) = true
  }
  val acc: AnotherContravariantCage[Cat] = new AnotherContravariantCage[Animal]
  acc.addAnimal(new Cat)
  class Kitty extends Cat
  acc.addAnimal(new Kitty)

  class MyList[+A] {
    def add[B >: A](element: B): MyList[B] = new MyList[B] // widening the type
  }

  val emptyList = new MyList[Kitty]
  val animals = emptyList.add(new Kitty)
  val moreAnimals = animals.add(new Cat)
  val evenMoreAnimals = moreAnimals.add(new Dog)

  // METHOD ARGUMENTS ARE IN CONTRAVARIANT POSITION.

  // return types
  class PetShop[-T] {
    // def get(isPuppy: Boolean): T // METHOD RETURN TYPES ARE IN COVARIANT POSITION
    /*
      val catShop = new PetShop[Animal] {
        def get(isPuppy: Boolean): Animal = new Cat
      }

      val dogShop: PetShop[Dog] = catshop
      dogShop.get(true)   // EVIL CAT!
    */

    def get[S <: T](isPuppy: Boolean, defaultAnimal: S): S = defaultAnimal
  }

  val shop: PetShop[Dog] = new PetShop[Animal]
  // val evilCat = shop.get(true, new Cat)
  class TerraNova extends Dog
  val bigFurry = shop.get(true, new TerraNova)

  /*
    Big rule
    - method arguments are in CONTRAVARIANT POSITION
    - return types are in COVARIANT POSITION
  */

  /**
   * 1. Invariant, covariant, contravariant
   *  Parking[T](list[T]) {
   *    park(vehicle: T)
   *    impound(vehicles: List[T])
   *    checkVehicles(condition: String): List[T]
   *  }
   *
   *  2. used someone else's API: InvariantList[T]
   *  3. Parking = monad!
   *      - flatMap
   */
  class Vehicle
  class Bike extends Vehicle
  class Car extends Vehicle
  class IList[T]

  class IParking[T](vehicles: List[T]){
    def park[T](vehicle: T): IParking[T] = ???
    def impound(vehicles: List[T]): IParking[T] = ???
    def checkVehicles(condition: String): List[T] = ???

    def flatMap[V](f: T => IParking[V]): IParking[V] = ???
  }

  class ConParking[-T](vehicles: List[T]) {
    def park[T](vehicle: T): ConParking[T] = ???
    def impound(vehicles: List[T]): ConParking[T] = ???
    def checkVehicles[V <: T](condition: String): List[V] = ???

    def flatMap[R <: T, V](f: R => ConParking[V]): ConParking[V] = ???
  }

  class CoParking[+T](vehicles: List[T]) {
    def park[V >: T](vehicle: V): CoParking[V] = ???
    def impound[V >: T](vehicles: List[V]): CoParking[V] = ???
    def checkVehicles(condition: String): List[T] = ???

    def flatMap[V](f: T => CoParking[V]): CoParking[V] = ???
  }

  /*
    Rule of thumb
    - use covariance = COLLECTION OF THINGS
    - use contravariance = GROUP OF ACTIONS
  */

  // 2
  class ConParking2[-T](vehicles: IList[T]) {
    def park(vehicle: T): ConParking2[T] = ???
    def impound[V <: T](vehicles: IList[V]): ConParking[V] = ???
    def checkVehicles[V <: T](condition: String): IList[V] = ???
  }

  class CoParking2[+T](vehicles: IList[T]) {
    def park[V >: T](vehicle: V): CoParking2[V] = ???
    def impound[V >: T](vehicles: IList[V]): CoParking2[V] = ???
    def checkVehicles[V >: T](condition: String): IList[V] = ???
  }

  // 3 - flatMap

}
