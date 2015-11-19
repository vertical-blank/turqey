
package com.fixeight.utils

import java.sql.Clob
import java.io.StringWriter

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

}

