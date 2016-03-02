package turqey.utils

import org.scalatest.FunSuite

class FileUtilSpec extends FunSuite {

  test("saveUserImage") {
    
    println(FileUtil.usrImageDir)
    
    val gifStr = "R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=";
    
    //val tmp = System.getProperty("java.io.tmpdir")
    
    val f = FileUtil.saveUserImage(gifStr)
    
    
  }

}
