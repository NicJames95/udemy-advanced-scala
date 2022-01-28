package lectures.part4implicits

object TypeClasses extends App {

  trait HTMLWritable {
    def toHtml: String
  }

  case class User(name: String, age: Int, email: String) extends HTMLWritable {
    override def toHtml: String = s"<div>$name ($age yo) <a href=$email/> </div>"
  }

  val john = User("John", 33, "john@rockthejvm.com").toHtml
  /*
    1 - for the types WE write
    2 - ONE implementation out of quite a number
  */

  // option 2 - pattern matching
  object HTMLSerializerPM {
    def serializeToHtml(value: Any) = value match {
      case User(n, a, e) =>
      case _ =>
    }
  }

  /*
    1 - lost type safety
    2 - need to modify the code every time
    3 still ONE implementation
  */

  trait HTMLSerializer[T] {
    def serialize(value: T): String
  }

  implicit object UserSerializer extends HTMLSerializer[User] {
    def serialize(user: User): String = s"<div>${user.name} (${user.age} yo) <a href=${user.email}/> </div> "
  }

  val julius = User("Julius", 18, "julius@rockthejvm.com")
  println(UserSerializer.serialize(julius))

  // 1 - we can define serializers for other types
  import java.util.Date
  object DateSerializer extends HTMLSerializer[Date] {
    def serialize(date: Date): String = s"<div>${date.toString()}</div>"
  }

  // 2 - we can define MULTIPLE serializers
  object PartialUserSerializer extends HTMLSerializer[User] {
    def serialize(user: User): String = s"<div>${user.name}</div>"
  }

  // part 2
  object HTMLSerializer {
    def serialize[T](value: T)(implicit serializer: HTMLSerializer[T]): String =
      serializer.serialize(value)

    def apply[T](implicit serializer: HTMLSerializer[T]):TypeClasses.HTMLSerializer[T] = serializer
  }

  implicit object IntSerializer extends HTMLSerializer[Int] {
    def serialize(value: Int): String = s"<div style: color=blue>$value</div>"
  }

  case class Admin(userName: String, email: String) extends HTMLWritable {
    def toHtml: String = s"<div>$userName <a href=$email/> </div>"
  }

  implicit object AdminSerializer extends HTMLSerializer[Admin] {
    def serialize(admin: Admin): String = s"<div> ${admin.userName} </div>"
  }

  val admin = Admin("Rockst4r", "rockst4r@rockthejvm.com")

  println(HTMLSerializer.serialize(42)(IntSerializer))
  println(HTMLSerializer.serialize(42))
  println(HTMLSerializer.serialize(admin))
  // println(HTMLSerializer.serialize(john)(UserSerializer)) doesn't work no implicit

  // access to the entire type class interface
  println(HTMLSerializer[Admin].serialize(admin))

  // part 3
  implicit class HTMLEnrichment[T](value: T) {
    def toHTML(implicit serializer: HTMLSerializer[T]): String = serializer.serialize(value)
  }

  println(julius.toHTML(UserSerializer)) // println(new HTMLEnrichment[User](john).toHTML(UserSerializer))
  println(julius.toHTML) // can call like this with implicit parameter since UserSerializer is an implicit object
  /*
    - extend to new types
    - choose implementation
    - super expressive
  */

  println(2.toHTML)
  println(julius.toHTML(PartialUserSerializer))

  /*
    - type class itself: HTMLSerializer[T] {...}
    - type class instances (some of which are implicit): UserSerializer, IntSerializer
    - conversion with implicit classes: HTMLEnrichment
  */

  // context bounds
  def htmlBoilerplate[T](content: T)(implicit serializer: HTMLSerializer[T]): String =
    s"<html><body> ${content.toHTML(serializer)}</body></html>"

  def htmlSugar[T : HTMLSerializer](content: T): String =
    val serializer = implicitly[HTMLSerializer[T]]
    // can now use serializer
    s"<html><body> ${content.toHTML}</body></html>"

  // implicitly
  case class Permissions(mask: String)
  implicit val defaultPermissions: Permissions = Permissions("0744")

  // in some other part of the code
  val standardPerms = implicitly[Permissions]

  /*
  Type class
  trait MyTypeClassTemplate[T] {
    def action(value: T): String
  }

  - Type class INSTANCES(often implicit)
  implicit object MyTypeClassInstance extends MyTypeClassTemplate[Int] { ... }

  - Invoking type class instances
  object MyTypeClassTemplate {
    def apply[T](implicit instance: MyTypeClassTemplate[T]) = instance
  }
  MyTypeClassTemplate[Int].action(2)

  - Enriching types with type classes
  implicit class ConversionClass[T](value: T) {
    def action(implicit instance: MyTypeClassTemplate[T]) = instance.action(value)
  }
  2.action
  */
}
