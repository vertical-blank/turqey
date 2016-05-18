package turqey.utils

import collection.JavaConversions._

import java.io.{File, InputStream, FileOutputStream, BufferedInputStream}
import java.nio.file.Files

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
  
  class Base64Decoder(data: String, name: Option[String] = None) extends MimeSupport {
    val Array(header, body): Array[String] = data.split(",")
    
    val binary: Array[Byte] = com.google.common.io.BaseEncoding.base64().decode(body)

    override def getName = this.name
    override def getInputStream = new java.io.ByteArrayInputStream(this.binary)

    def saveTo(file: File): File = writeBinary(binary, file)
    
    def saveAsUserImage(name: String): File = saveTo(new File(usrImageDir, name))
    
  }
  
  import org.scalatra.servlet.FileItem
  class FileSaver(item: FileItem) extends MimeSupport {
    override def getName = Some(item.getName)
    override def getInputStream = item.getInputStream
    
    def saveTo(file: File): File = {
      item.write(file)
      file
    }
    
    def saveUpload(name: String): File = saveTo(new File(uploadsDir, name))
    
  }

  trait MimeSupport {
    import org.apache.tika._
    import org.apache.tika.detect._
    import org.apache.tika.mime._
    import org.apache.tika.metadata._

    def getName:        Option[String]
    def getInputStream: java.io.InputStream

    lazy val mime = {
      val md = new Metadata() 
      this.getName.foreach(md.set(TikaMetadataKeys.RESOURCE_NAME_KEY, _))
      val detector = new DefaultDetector()
      detector.detect(new BufferedInputStream(this.getInputStream), md)
    }
    
    lazy val mimeType: String = this.mime.toString
    
    lazy val isImage: Boolean = this.mime.getType == "image"
    
  }
  
  def writeBinary(bin: Array[Byte], file: File): File = {
    val out = new FileOutputStream(file)
    out.write(bin)
    out.close
    file
  }
  
  def getByteArray(file: File): Array[Byte] = Files.readAllBytes(file.toPath)

}

