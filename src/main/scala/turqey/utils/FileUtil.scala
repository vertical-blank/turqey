package turqey.utils

import collection.JavaConversions._
import eu.medsea.mimeutil._

import java.io.File
import java.io.InputStream

object FileUtil {
  
  val envKey = "TURQEY_HOME"
  
  lazy val homeDir = Option(System.getenv(envKey)).getOrElse{ new File(System.getProperty("user.home")).getAbsolutePath() + "/.turqey/" }
  
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
  
  def saveFileTo(file: File, path: String): Unit = {
    
  }

}

