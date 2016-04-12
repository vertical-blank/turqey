package turqey.controller

import org.scalatra._
import org.scalatra.ScalatraServlet
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig, SizeConstraintExceededException}
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.utils.Json
import turqey.utils.Implicits._

class UploadController extends AuthedController with FileUploadSupport {
  configureMultipartHandling(MultipartConfig(maxFileSize = Some(3*1024*1024)))

  override val path = "upload"

  post("/attach/"){ implicit dbSession =>
    contentType = "application/json"

    val user = turqey.servlet.SessionHolder.user.get

    val result = fileParams.get("file").map { f =>
      val filename = params.get("name").getOrElse("%tY/%<tm/%<td_%<tH:%<tM:%<tS" format new java.util.Date())
      val saver = new FileUtil.FileSaver(f)
      val newRec = Upload.create(
        name      = filename,
        mime      = saver.mimeType,
        isImage   = saver.isImage,
        ownerId   = user.id
      )
      saver.saveUpload(newRec.id.toString)
      newRec
    }
    
    Json.toJson(result)
  }

  getWithoutDB("/attach/:id/") {
    val user = turqey.servlet.SessionHolder.user.get

    val id = params.get("id").getOrElse(redirectFatal("/")).toLong

    Upload.find(id).map{ rec =>
      contentType = rec.mime
      response.setHeader("Content-Disposition", "attachment; filename=" + rec.name)
      new java.io.File(FileUtil.uploadsDir, id.toString)
    } getOrElse( resourceNotFound() )
  }

}

