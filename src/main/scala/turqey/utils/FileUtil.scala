package turqey.utils

import collection.JavaConversions._
import eu.medsea.mimeutil._

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

}

