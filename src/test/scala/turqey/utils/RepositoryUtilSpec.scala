package turqey.utils

import org.scalatest.FunSuite
import scala.concurrent.Future

class RepositoryUtilSpec extends FunSuite {
  import scala.concurrent.ExecutionContext.Implicits.global
  import NamedLock.withLock

  test("lock") {
    val key1 = "aaa"
    val key2 = "aaaa".substring(1)

    // same value, but another instance.
    assert( key1 == key2 )
    assert( key1 ne key2 )
    
    // must locked by value.
    withLock(key1) {
      val vals1 = scala.collection.mutable.ListBuffer.empty[Int]

      Future {
        withLock(key2) {
          val vals2 = Range(0, 100).map( _ * 2)
            assert(vals1 == vals2)
        }
      }

      Range(0, 100).foreach{
        Thread.sleep(100)
        vals1 += _ * 2
      }
    }
  }

  test("noLock") {
    val key1 = "aaa"
    val key2 = "bbb"

    // must *Not* locked
    withLock(key1) {
      val vals1 = scala.collection.mutable.ListBuffer.empty[Int]

      Future {
        withLock(key2) {
          val vals2 = Range(0, 100).map( _ * 2)
            assert(vals1 != vals2)
        }
      }

      Range(0, 100).foreach{
        Thread.sleep(100)
        vals1 += _ * 2
      }
    }
  }
  
}

