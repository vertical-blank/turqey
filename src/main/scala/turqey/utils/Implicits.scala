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

  implicit class StringImprovements(val s: String) {
    import scala.util.control.Exception._

    def intOpt : Option[Int]  = catching(classOf[NumberFormatException]) opt s.toInt
    def longOpt: Option[Long] = catching(classOf[NumberFormatException]) opt s.toLong
  }

}

