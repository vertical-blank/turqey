package turqey.utils

import org.scalatest.FunSuite
import turqey.utils.Json._

case class CaseClass(str: String, nums: Seq[Int])

class JsonSpec extends FunSuite {

  test("parseToCaseClass") {
    val json = """
      |{
      |  "str"  : "STR",
      |  "nums" : [1, 2, 3]
      |}""".stripMargin
    
    val parsed = Json.parseAs[CaseClass](json)
    
    assert( parsed == CaseClass("STR", Seq(1, 2, 3)) )
  }
  
}
