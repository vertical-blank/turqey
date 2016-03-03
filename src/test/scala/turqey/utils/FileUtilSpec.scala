package turqey.utils

import org.scalatest.FunSuite
import turqey.utils.FileUtil._

class FileUtilSpec extends FunSuite {

  test("saveAndRestoreImage") {
    
    val gifStr = "data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs="
    
    val img = new Base64Image(gifStr)
    
    assert(img.mediaType == "image/gif")
    
    val file = new java.io.File(System.getProperty("java.io.tmpdir"), "tmp.gif")
    img.saveTo(file)
    
    val bytes = java.nio.file.Files.readAllBytes(file.toPath)
    val encodeStr = com.google.common.io.BaseEncoding.base64().encode(bytes)
    file.delete
    
    assert(encodeStr == img.body)
  }
  
}
