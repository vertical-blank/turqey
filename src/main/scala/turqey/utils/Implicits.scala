
package turqey.utils

import java.sql.Clob
import java.io.StringWriter
import java.io.BufferedReader
import scalikejdbc._

import scala.language.implicitConversions

object Implicits {

  implicit def clobToString(clob: Clob):String = {
    clob.getSubString(1, clob.length.asInstanceOf[Int])
  }

  implicit def stringToClob(string: String):Clob = {
    using(ConnectionPool.borrow()) { conn =>
      val clob = conn.createClob
      clob.setString(1, string)
      clob
    }
  }

}

