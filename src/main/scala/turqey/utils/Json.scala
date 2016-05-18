package turqey.utils

import org.json4s._
import org.json4s.native.JsonMethods
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.Extraction._
import org.json4s.native.Serialization
import org.json4s.JsonAST.JValue

object Json {
  implicit val formats = DefaultFormats
  
  def toJson(value: AnyRef):String = Serialization.write(value)
  
  def parseAs[T](json: String)(implicit m: scala.reflect.Manifest[T]): T = JsonMethods.parse(json).extract[T]
  
}

