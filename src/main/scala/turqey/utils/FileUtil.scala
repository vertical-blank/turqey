package turqey.utils

import collection.JavaConversions._

import java.io.{File, InputStream, FileOutputStream}

object FileUtil {
  
  val envKey = "TURQEY_HOME"
  
  lazy val homeDir = Option(System.getenv(envKey))
    .getOrElse{ new File(System.getProperty("user.home")).getAbsolutePath() + "/.turqey" }
  
  lazy val usrImageDir    = getDir(new File(homeDir, "userImage"))
  lazy val articleBaseDir = getDir(new File(homeDir, "articles"))
  lazy val uploadsDir     = getDir(new File(homeDir, "uploads"))
  
  def getDir(dir: File) = {
    if (!dir.exists()){
      dir.mkdir()
    }
    dir
  }
  
  class Base64Decoder(data: String, name: Option[String] = None) {
    val Array(header, body): Array[String] = data.split(",")
    
    val binary: Array[Byte] = com.google.common.io.BaseEncoding.base64().decode(body)

    import org.apache.tika._
    import org.apache.tika.detect._
    import org.apache.tika.mime._
    import org.apache.tika.metadata._

    lazy val mime = {
      val md = new Metadata() 
      this.name.foreach( md.set(TikaMetadataKeys.RESOURCE_NAME_KEY, _) )
      val detector = new DefaultDetector()
      detector.detect(new java.io.ByteArrayInputStream(binary), md)
    }
    
    lazy val mimeType: String = this.mime.toString
    
    lazy val isImage: Boolean = this.mime.getType == "image"
    
    def saveTo(file: File): File = {
      writeBinary(binary, file)
    }
    
    def saveUpload(name: String): File = {
      saveTo(new File(uploadsDir, name))
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
  

}

