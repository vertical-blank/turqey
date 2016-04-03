package turqey.utils

import scala.collection._
import scala.collection.convert.decorateAsScala._
import java.util.concurrent.locks._

object LockByVal {

  val objs: concurrent.Map[Any, Lock] = new java.util.concurrent.ConcurrentHashMap().asScala

  def lock(key: Any): Lock = {
    objs.putIfAbsent(key, new ReentrantLock())
    objs(key)
  }

  def withLock[T](key: Any)(f: => T) = {
    try {
      lock(key).lock
      f
    }
    finally {
      lock(key).unlock
    }
  }
}

