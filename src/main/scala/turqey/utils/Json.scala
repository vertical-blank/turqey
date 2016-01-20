package turqey.utils

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.Extraction._
import org.json4s.native.Serialization 

object Json {
  
  def toJson(value: AnyRef):String = {
    implicit val formats = DefaultFormats
    Serialization.write(value)
  }
  
}

