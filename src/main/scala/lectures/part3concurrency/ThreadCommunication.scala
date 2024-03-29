package lectures.part3concurrency

import scala.collection.mutable
import scala.util.Random

object ThreadCommunication extends App {

  /*
    the producer-consumer problem

    producer -> [ ? ] -> consumer
  */
  class SimpleContainer {
    private var value: Int = 0

    def isEmpty: Boolean = value == 0
    def set(newValue: Int): Unit = value = newValue
    def get: Int = {
      val result = value
      value = 0
      result
    }
  }

  def naiveProdCons(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting...")
      while(container.isEmpty) {
        println("[consumer] actively waiting...")
      }

      println("[consumer] I have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("[producer] computing...")
      Thread.sleep(500)
      val value = 42
      println("[producer] I have produced, after long work, the value " + value)
      container set value
    })

    /*
      Note: this test assumes the consumer starts first and waits for the producer

      It may be the case that the producer may start and finish first,
      and the consumer is stuck waiting.
      This simple implementation does not include that case.
    */
    consumer.start()
    producer.start()
  }

  // naiveProdCons()

  /* SYNCHRONIZED
   * Entering a synchronized expression on an object LOCKS THE OBJECT:
      val someObject = "hello"

      someObject.synchronized { <<------------------- lock the object's MONITOR
        // code <<----------------------------------- any other thread trying to run this will block
      } <<------------------------------------------- release the lock

      Only AnyRefs can have synchronized blocks.

      General principles:
        - make no assumptions about who gets the lock first
        - keep locking to a minimum
        - maintain THREAD SAFETY at ALL TIMES in parallel applications

    _*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*

      WAIT() AND NOTIFY()
      wait()-ing on an object's monitor suspends you (the thread) indefinitely

      // thread 1
      val someObject = "hello"
      someObject.synchronized { <<------------ lock the object's MONITOR
        // ... code part 1
      someObject.wait() <<-------------------- release the LOCK and ... wait
        // ... code part 2 <<---------- when allowed to proceed, lock the monitor again and continue
      }

      // thread 2
      someOjbect.synchronized { <<------------ lock the object's MONITOR
        // ... code
      someObject.notify() <<----------- signal ONE sleeping thread they may continue (WHICH thead? You don't know!)
        // ... more code                                                ^^^^ Use notifyAll() to awaken ALL threads
      } <<------------------------- but only after I'm done and unlock the monitor

      Waiting and notifying only work in SYNCHRONIZED expressions.
   * */

  // wait and notify
  def smartProdCons(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting...")
      container.synchronized {
        container.wait()
      }

      // container must have some value
      println("[consumer] I have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("[producer] Hard at work...")
      Thread.sleep(2000)
      val value = 42

      container.synchronized {
        println("[producer I'm producing " + value)
        container.set(value)
        container.notify()
      }
    })

    consumer.start()
    producer.start()
  }

  // smartProdCons()

  /*
    producer -> [ ? ? ? ] -> consumer
  */

  def prodConsLargeBuffer(): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity = 3

    val consumer = new Thread(() => {
      val random = new Random()

      while (true) {
        buffer.synchronized {
          if (buffer.isEmpty) {
            println("[consumer] buffer empty, waiting...")
            buffer.wait()
          }

          // there must be at least ONE value in the buffer
          val x = buffer.dequeue()
          println("[consumer] consumed " + x)

          // hey producer, there's empty space available, are you lazy?
          buffer.notify()
        }

        Thread.sleep(random.nextInt(500))
      }
    })

    val producer = new Thread(() => {
      val random = new Random()
      var i = 0

      while (true) {
        buffer.synchronized {
          if (buffer.size == capacity) {
            println("[producer] buffer is full, waiting...")
            buffer.wait()
          }

          // there must be at least ONE EMPTY SPACE in the buffer
          println("[producer] producing " + i)
          buffer.enqueue(i)

          // hey consumer, new food for you!
          buffer.notify()

          i += 1
        }

        Thread.sleep(random.nextInt(500))
      }
    })

    consumer.start()
    producer.start()
  }

  //prodConsLargeBuffer()

  /*
  Prod-cons, level 3

    producer1 -> [ ? ? ? ] -> consumer1
    producer2 ----^     ^---- consumer2
*/
  class Consumer(id: Int, buffer: mutable.Queue[Int]) extends Thread {
    override def run(): Unit = {
      val random = new Random()

      while (true) {
        buffer.synchronized {
          /*
            producer produces value, two Consumers are waiting
            notifies ONE consumer, notifies on buffer
            notifies the other consumer
          */
          while (buffer.isEmpty) {
            println(s"[consumer $id] buffer empty, waiting...")
            buffer.wait()
          }

          // there must be at least ONE value in the buffer
          val x = buffer.dequeue() // OOps.!
          println(s"[consumer $id] consumed " + x)


          buffer.notify()
        }

        Thread.sleep(random.nextInt(500))
      }
    }
  }

  class Producer(id: Int, buffer: mutable.Queue[Int], capacity: Int) extends Thread {
    override def run(): Unit = {
      val random = new Random()
      var i = 0

      while (true) {
        buffer.synchronized {
          while (buffer.size == capacity) {
            println(s"[producer $id] buffer is full, waiting...")
            buffer.wait()
          }

          // there must be at least ONE EMPTY SPACE in the buffer
          println(s"[producer $id] producing " + i)
          buffer.enqueue(i)


          buffer.notify()

          i += 1
        }

        Thread.sleep(random.nextInt(700))
      }
    }
  }

  def multiProdCons(nConsumers: Int, nProducers: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity = 20

    (1 to nConsumers).foreach(i => new Consumer(i, buffer).start())
    (1 to nProducers).foreach(i => new Producer(i, buffer, capacity).start())
  }

  // multiProdCons(3, 6)

  /*
    notifyAll()
      The behavior does not change in terms of RACING,
      but using notifyAll prevents a possible deadlock.

    Example: 10 producers, 2 consumers, buffer size = 3.

    1. One producer fills the buffer quickly. The other 9 go to sleep.
    Same with the open producer when it's done.
    2. One consumer consumes all, then goes to sleep.
    The others go to sleep once they see the buffer empty.
    3. After 3 notifications, 3 producers wake up, fill the space.
    Notifications go to the other producers.
    4. Every poor producer sees buffer full, goes to sleep.
    5. Deadlock.

    NotifyAll fixes this.
  */

  /*
    Exercises.
    1) think of an example where notifyAll acts in a different way than notify
    2) create a deadlock
    3) create a livelock
  */

  // notifyAll
  def testNotifyAll(): Unit = {
    val bell = new Object

    (1 to 10).foreach(i => new Thread(() => {
      bell.synchronized {
        println(s"[thread $i] waiting...")
        bell.wait()
        println(s"[thread $i] hooray!")
      }
    }).start())

    new Thread(() => {
      Thread.sleep(2000)
      println("[announcer] Rock'n roll!")
      bell.synchronized {
        bell.notifyAll()
      }
    }).start()
  }

  // testNotifyAll()

  // 2 - deadlock
  case class Friend(name: String) {
    def bow(other: Friend): Unit = {
      this.synchronized {
        println(s"$this: I am bowing to my friend $other")
        other.rise(this)
        println(s"$this: my friend $other has risen")
      }
    }

    def rise(other: Friend): Unit = {
      this.synchronized {
        println(s"$this: I am rising to my friend $other")
      }
    }

    var side = "right"
    def switchSide(): Unit = {
      if (side == "right") side = "left"
      else side = "right"
    }

    def pass(other: Friend): Unit = {
      while (this.side == other.side) {
        println(s"$this: Oh, but please, $other, feel free to pass...")
        switchSide()
        Thread.sleep(1000)
      }
    }
  }

  val sam = Friend("Sam")
  val pierre = Friend("Pierre")

  // new Thread(() => sam.bow(pierre)).start() // sam's lock,     | then pierre's lock
  // new Thread(() => pierre.bow(sam)).start() // pierre's lock,  | then sam's lock

  // 3 - livelock
  new Thread(() => sam.pass(pierre)).start()
  new Thread(() => pierre.pass(sam)).start()
}




