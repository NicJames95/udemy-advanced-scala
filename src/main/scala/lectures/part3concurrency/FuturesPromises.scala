package lectures.part3concurrency

import scala.util.{Failure, Random, Success, Try}
import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration.*
// important for futures
import scala.concurrent.ExecutionContext.Implicits.global

/* BACK TO THE FUTURE[T]
  - Future[T] is a computation which will finish AT SOME POINT

   import ExecutionContext.Implicits.global <----------------------- a default ExecutionContext already implemented

   val recipesFuture: Future[List[Recipe]] = Future {
   // some code that takes a long time to run
   jamieOliverDb.getAll("chicken")
   } <-------------------------------------------------------------- ec is passed implicitly

  - non-blocking processing   future.onComplete { case Success(recipes) => ... }

  - map, flatMap, filter, for-comprehensions

  - falling back      future.recover { case NotFoundException => ... }

  - blocking if need be     val txStatus = Await.result(transaction, 1 seconds)

 * MAKING PROMISES
  - Futures are immutable, "read-only" objects.
  - PROMISES are "writable-once" containers over a future.

  thread 1:                                              thread 2:
  - creates an empty promise                             - holds the promise
  - knows how to handle the result                       - fulfills or fails the promise

                                                         val result = doComputation()
  val p = Promise[Int]()                                 p.success(result)
  val future = p.future(promise wraps future
                        future is "undefined")           OR

  future.onComplete { <-------triggers completion------- p.failure(new BadException(...))
    case Success(value) => ...
    case Failure(ex) => ...                              OR

                                                         p.complete(Try {...})

*/

object FuturesPromises extends App {

  def calculateMeaningOfLife: Int = {
    Thread.sleep(2000)
    42
  }

  val aFuture = Future {
    calculateMeaningOfLife // calculates the meaning of life on ANOTHER thread
  } // (global) which is passed by the compiler

  println(aFuture.value) // Option[Try[Int]]

  println("Waiting on the future")
//  aFuture.onComplete(t => t match {
//    case Success(meaningOfLife) => println(s"the meaning of life is $meaningOfLife")
//    case Failure(e) => println(s"I have failed with $e")
//  })

  // Using partial function instead of case match above
  aFuture.onComplete {
    case Success(meaningOfLife) => println(s"the meaning of life is $meaningOfLife")
    case Failure(e) => println(s"I have failed with $e")
  } // SOME thread

  Thread.sleep(3000)

  // mini social network

  case class Profile(id: String, name: String) {
    def poke(anotherProfile: Profile): Unit =
      println(s"${this.name} poking ${anotherProfile.name}")
  }

  object SocialNetwork {
    // "database"
    val names = Map(
      "fb.id.1-zuck" -> "Mark",
      "fb.id.2-bill" -> "Bill",
      "fb.id.0-dummy" -> "Dummy"
    )

    val friends = Map(
      "fb.id.1-zuck" -> "fb.id.2-bill"
    )

    val random = new Random()

    // API
    def fetchProfile(id: String): Future[Profile] = Future {
      // fetching from the DB
      Thread.sleep(random.nextInt(300))
      Profile(id, names(id))
    }

    def fetchBestFriend(profile: Profile): Future[Profile] = Future {
      Thread.sleep(random.nextInt(400))
      val bfId = friends(profile.id)
      Profile(bfId, names(bfId))
    }
  }

  // client: mark to poke bill
  val mark = SocialNetwork.fetchProfile("fb.id.1-zuck")
//  mark.onComplete {
//    case Success(markProfile) => {
//      val bill = SocialNetwork.fetchBestFriend(markProfile)
//      bill.onComplete {
//        case Success(billProfile) => markProfile.poke(billProfile)
//        case Failure(e) => e.printStackTrace()
//      }
//    }
//    case Failure(ex) => ex.printStackTrace()
//  }



  // functional composition of futures
  // map, flatMap, filter
  val nameOnTheWall = mark.map(profile => profile.name)
  val marksBestFriend = mark.flatMap(profile => SocialNetwork.fetchBestFriend(profile))
  val zucksBestFriendRestricted = marksBestFriend.filter(profile => profile.name.startsWith("z"))

  // for-comprehensions
  for {
    mark <- SocialNetwork.fetchProfile("fb.id.1-zuck")
    bill <- SocialNetwork.fetchBestFriend(mark)
  } yield mark.poke(bill)

  for {
    mark <- SocialNetwork fetchProfile("fb.id.1-zuck")
    bill <- SocialNetwork fetchBestFriend(mark)
  } mark poke bill

  Thread.sleep(1000)

  // fallbacks
  val aProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recover {
    case e: Throwable => Profile("fb.id.0-dummy", "Forever alone")
  }

  val aFetchedProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recoverWith {
    case e: Throwable => SocialNetwork.fetchProfile("fb.id.0-dummy")
  }

  val fallbackResult = SocialNetwork.fetchProfile("unknown id").fallbackTo(SocialNetwork.fetchProfile("fb.id.0-dummy"))

  // online banking app
  case class User(name: String)
  case class Transaction(sender: String, receiver: String, amount: Double, status: String)

  object BankingApp {
    val name = "Rock the JVM banking"

    def fetchUser(name: String): Future[User] = Future {
      // simulate fetching from the DB
      Thread.sleep(500)
      User(name)
    }

    def createTransaction(user: User, merchantName: String, amount: Double): Future[Transaction] = Future {
      // simulate some processes
      Thread.sleep(1000)
      Transaction(user.name, merchantName, amount, "SUCCESS")
    }

    def purchase(userName: String, item: String, merchantName: String, cost: Double): String = {
      // fetch the user from the DB
      // create a transaction
      // WAIT for the transaction to finish
      val transactionStatusFuture = for {
        user <- fetchUser(userName)
        transaction <- createTransaction(user, merchantName, cost)
      } yield transaction.status

      Await.result(transactionStatusFuture, 2.seconds) // implicit conversions -> pimp my library
    }
  }

  println(BankingApp.purchase("Daniel", "iPhone12", "Rock The JVM", 3000))

  // promises

  val promise = Promise[Int]() // "controller" over a future
  val future = promise.future

  // thread 1 - "consumer"
  future.onComplete {
    case Success(r) => println("[consumer] I've received " + r)
  }

  // thread 2 - "producer"
  val producer = new Thread(() => {
    println("[producer] crunching numbers...")
    Thread.sleep(1000)
    // "fulfilling" the promise
    promise.success(42)
    println("[producer] done")
  })

  producer.start()
  Thread.sleep(500)

  /*
    1) fulfill a future IMMEDIATELY with a value
    2) inSequence(fa, fb)
    3) first(fa, fb) => new future with the first value of the two futures
    4) last(fa, fb) => new future with the last value
    5) retryUntil(action: () => Futrue[T], condition: T => Boolean): Future[T]
  */

  // 1 - fulfill immediately
  // Alternative: Future.successful(value)
  // It's actually even faster because the Future is fulfilled synchronously (so no thread is needed).
  def fulfillImmediately[T](value: T): Future[T] = Future successful(value)
  // 2 - inSequence
  def inSequence[A, B](first: Future[A], second: Future[B]): Future[B] =
    first.flatMap(_ => second)

  // 3 - first out of two futures
  def first[A](fa: Future[A], fb: Future[A]): Future[A] = {
    val promise = Promise[A]

    def tryComplete(promise: Promise[A], result: Try[A]) = result match {
      case Success(r) => try {
        promise.success(r)
      } catch {
        case _ =>
      }
      case Failure(t) => try {
        promise.failure(t)
      } catch {
        case _ =>
      }
    }
    fa.onComplete(result => tryComplete(promise, result))
    fb.onComplete(promise.tryComplete) // existing try complete function apart of promise

    promise.future
  }

  // 4 - last out of the two futures
  def last[A](fa: Future[A], fb: Future[A]): Future[A] = {
    // 1 promise which both futures will try to complete
    // 2 promise which the LAST future will complete
    val bothPromise = Promise[A]
    val lastPromise = Promise[A]
    val checkAndComplete = (result: Try[A]) =>
      if(!bothPromise.tryComplete(result))
        lastPromise.complete(result)

    fa.onComplete(checkAndComplete)
    fb.onComplete(checkAndComplete)

    lastPromise.future
  }

  val fast = Future {
    Thread.sleep(250)
    42
  }

  val slow = Future {
    Thread.sleep(500)
    24
  }
  first(fast, slow) foreach println
  last(fast, slow).foreach(println)

  Thread.sleep(1000)


  // 5 - retryUntil
  def retryUntil[A](action: () => Future[A], condition: A => Boolean): Future[A] =
    action()
    .filter(condition)
    .recoverWith {
      case _ => retryUntil(action, condition)
    }

  val random = new Random()
  val action = () => Future {
    Thread.sleep(1000)
    val nextValue = random.nextInt(100)
    println("generated " + nextValue)
    nextValue
  }

  retryUntil(action, (x: Int) => x < 50).foreach(result => println("settled at " + result))
  Thread.sleep(10000)
}
