package turqey.utils

import collection.JavaConversions._
import eu.medsea.mimeutil._

import java.io.{File, InputStream, FileOutputStream}

object FileUtil {
  
  val envKey = "TURQEY_HOME"
  
  lazy val homeDir = Option(System.getenv(envKey)).getOrElse{ new File(System.getProperty("user.home")).getAbsolutePath() + "/.turqey" }
  
  lazy val usrImageDir = {
    val dir = new File(homeDir, "userImage")
    if (!dir.exists()){
      dir.mkdir()
    }
    dir
  }
  
  def saveUserImage(base64Image: String): File = {
    import com.google.common.io.BaseEncoding
    
    val binary = BaseEncoding.base64().decode(base64Image)
    
    val fileId = "1"
    
    val f = new File(usrImageDir, fileId)
    val out = new FileOutputStream(f)
    out.write(binary)
    out.close
    f
  }
  
  def saveFileTo(file: File, path: String): Unit = {
    
  }
  
  def getMimeType(file: java.io.File): String = {
    MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    val mimeTypes = MimeUtil.getMimeTypes(file);
    
    mimeTypes.headOption match {
      case Some(mimeType: MimeType)
        => mimeType.getMediaType() + "/" + mimeType.getSubType()
      case _
        => "application/octet-stream"
    }
  }
  
  def isImage(file: java.io.File): Boolean = {
    MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    val mimeTypes = MimeUtil.getMimeTypes(file);
    
    mimeTypes.headOption match {
      case Some(mimeType: MimeType)
        => mimeType.getMediaType() == "image"
      case _
        => false
    }
  }

}

