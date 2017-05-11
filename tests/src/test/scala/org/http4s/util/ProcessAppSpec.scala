package org.http4s
package util

import org.http4s.util.task.never
import scalaz.concurrent.Task
import scalaz.stream.async
import scalaz.stream.async.mutable.Signal
import scalaz.stream.Process

class ProcessAppSpec extends Http4sSpec {

  "ProcessApp" should {

    /**
      * Simple Test Rig For Process Apps
      * Takes the Process that constitutes the Process App
      * and observably cleans up when the process is stopped.
      *
      */
    class TestProcessApp(process: Process[Task, Nothing]) extends ProcessApp {
      val cleanedUp : Signal[Boolean] = async.signalOf(false)
      override def process(args: List[String]): Process[Task, Nothing] = {
        process.onComplete(Process.eval_(cleanedUp.set(true)))
      }
    }

    "Terminate Server on a Process Failure" in {
      val testApp = new TestProcessApp(
        Process.fail(new Throwable("Bad Initial Process"))
      )
      testApp.doMain(Array.empty[String]) should_== -1
      testApp.cleanedUp.get.unsafePerformSync should_== true
    }

    "Terminate Server on a Valid Process" in {
      val testApp = new TestProcessApp(Process.halt)
      testApp.doMain(Array.empty[String]) should_== 0
      testApp.cleanedUp.get.unsafePerformSync should_== true
    }

    "requestShutdown Shuts Down a Server From A Separate Thread" in {
      val testApp = new TestProcessApp(
        // run forever, emit nothing
        Process.eval_(task.never)
      )
      val runApp = Task.unsafeStart(testApp.doMain(Array.empty[String]))
      testApp.requestShutdown.unsafePerformSync
      runApp.unsafePerformSync should_== 0
      testApp.cleanedUp.get.unsafePerformSync should_== true
    }
  }
}
