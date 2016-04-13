package turqey.utils

import org.scalatest.FunSuite
import turqey.utils.FileUtil._

class FileUtilSpec extends FunSuite {
  val base64 = com.google.common.io.BaseEncoding.base64()

  test("saveAndRestoreImage") {
    
    val gifStr = "data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs="
    
    val img = new Base64Decoder(gifStr)
    assert(img.mimeType == "image/gif")
    assert(img.isImage == true)

    val file = new java.io.File(System.getProperty("java.io.tmpdir"), "tmp.gif")
    img.saveTo(file)
    
    val bytes = java.nio.file.Files.readAllBytes(file.toPath)
    file.delete
    
    assert(java.util.Arrays.equals(bytes, img.binary))
  }

  test("textMimeType") {
    val jsonBytes = """
      |{
      |  "a": 123,
      |  "b": "xxx"
      |}
    """.stripMargin.getBytes
    
    val json = new Base64Decoder("data: :base64," + base64.encode(jsonBytes), Some("hoge.json"))
    assert(json.mimeType == "application/json")
    assert(json.isImage == false)
    
  }
  
}
