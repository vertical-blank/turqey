
package turqey.utils

import java.sql.Clob
import java.io.StringWriter
import scalikejdbc._

import scala.language.implicitConversions

object Implicits {

  implicit def clobToString(clob: Clob):String = {
    val buff = new Array[Char](clob.length.asInstanceOf[Int])

    val reader = clob.getCharacterStream
    reader.read(buff)
    val sw = new StringWriter()
    sw.write(buff)
    reader.close

    sw.toString
  }

  implicit def stringToClob(string: String):Clob = {
    using(ConnectionPool.borrow()) { conn =>
      val clob = conn.createClob
      clob.setString(1, string)
      clob
    }
  }

}

