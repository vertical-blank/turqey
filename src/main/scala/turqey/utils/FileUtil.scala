package turqey.utils

import collection.JavaConversions._
import eu.medsea.mimeutil._

import java.io.{File, InputStream, FileOutputStream}

object FileUtil {
  
  val envKey = "TURQEY_HOME"
  
  lazy val homeDir = Option(System.getenv(envKey))
    .getOrElse{ new File(System.getProperty("user.home")).getAbsolutePath() + "/.turqey" }
  
  lazy val usrImageDir = getDir(new File(homeDir, "userImage"))
  
  def getDir(dir: File) = {
    if (!dir.exists()){
      dir.mkdir()
    }
    dir
  }
  
  class Base64Image(data: String) {
    val Array(header, body) = data.split(",")
    
    def left( str: String, char: Character) :String = str.substring(0, str.indexOf(char))
    def right(str: String, char: Character) :String = str.substring(str.lastIndexOf(char) + 1)
    def mid(str: String, begin: Character, end: Character) = left(right(str, begin), end) 
    
    val mediaType = mid(header, ':', ';')
    val binary = com.google.common.io.BaseEncoding.base64().decode(body)
    
    def saveTo(file: File): File = {
      writeBinary(binary, file)
    }
    
    def saveAsUserImage(name: String): File = {
      saveTo(new File(usrImageDir, name))
    }
    
  }
  
  def writeBinary(bin: Array[Byte], file: File): File = {
    val out = new FileOutputStream(file)
    out.write(bin)
    out.close
    file
  }
  
  def getByteArray(file: File): Array[Byte] = {
    java.nio.file.Files.readAllBytes(file.toPath)
  }
  
  MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector")
  
  def getMimeType(file: File): String = {
    val mimeTypes = MimeUtil.getMimeTypes(file);
    
    mimeTypes.headOption match {
      case Some(mimeType: MimeType)
        => mimeType.getMediaType() + "/" + mimeType.getSubType()
      case _
        => "application/octet-stream"
    }
  }
  
  def isImage(file: File): Boolean = {
    val mimeTypes = MimeUtil.getMimeTypes(file);
    
    mimeTypes.headOption match {
      case Some(mimeType: MimeType)
        => mimeType.getMediaType() == "image"
      case _
        => false
    }
  }

}

