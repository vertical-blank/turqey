package turqey.utils

import collection.JavaConversions._
import eu.medsea.mimeutil._

import java.io.File
import java.io.InputStream

object FileUtil {

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

