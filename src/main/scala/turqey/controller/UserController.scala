package turqey.controller

import org.scalatra._
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig, SizeConstraintExceededException}
import javax.servlet.http.HttpServletRequest
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet._

class UserController extends AuthedController
  with FileUploadSupport
  with ScalateSupport {
  configureMultipartHandling(MultipartConfig(maxFileSize = Some(3*1024*1024)))
  
  override val path = "user"
  
  val pagesize = 20

  val list = get("/") { implicit dbSession =>
    jade("/user/list",
      "users" -> User.findAll())
  }

  val view = get("/:id"){ implicit dbSession =>
    val id = params.get("id").getOrElse(redirectFatal("/")).toLong

    val articleIds = Article.findAllIdBy(
      sqls.eq(Article.column.ownerId, id)
    ).grouped(pagesize).toSeq

    //show user detail
    jade("/user/view",
      "u" -> User.find(id).getOrElse(redirectFatal("/")),
      "articleIds" -> articleIds
    )
  }

  val edit = get("/:id/edit"){ implicit dbSession =>
    val id = params.get("id").getOrElse(redirectFatal("/")).toLong
    val user = User.find(id).getOrElse(redirectFatal("/"))
    
    if(!user.editable){ redirectFatal("/") }

    jade("/user/edit", "u" -> Some(user))
  }

  val editNew = get("/edit"){ implicit dbSession =>
    if(!SessionHolder.root){ redirectFatal("/") }
    
    jade("/user/edit", "u" -> None)
  }

  post("/:id"){ implicit dbSession =>
    val id = params.get("id").getOrElse(redirectFatal("/")).toLong
    val user = User.find(id).getOrElse(redirectFatal("/"))
    
    if(!user.editable){ redirectFatal("/") }

    val updUsr = user.copy(
      name     = params.get("name").get,
      email    = params.get("email").get,
      password = params.get("password") match {
        case Some("") | None => { user.password }
        case Some(p)  => { Digest.get(p) }
      },
      imgUrl   = "",
      root     = SessionHolder.root && params.get("root").isDefined
    ).save()

    val sessionUsr = SessionHolder.user.get
    if (sessionUsr.id == user.id){
      session("user") = sessionUsr.copy(name = updUsr.name, imgUrl = updUsr.imgUrl)
    }

    redirect(url(view, "id" -> id.toString))
  }

  post("/:id/reset"){ implicit dbSession =>
    val id = params.get("id").getOrElse(redirectFatal("/")).toLong
    val user = User.find(id).getOrElse(redirectFatal("/"))
    
    if(!user.editable){ redirectFatal("/") }

    user.copy(password = Digest.get(user.loginId) ).save()

    redirect(url(view, "id" -> id.toString))
  }

  post("/"){ implicit dbSession =>
    if(!SessionHolder.root){ redirectFatal("/") }
    
    val id = User.create(
      loginId  = params.get("loginId").get,
      name     = params.get("name").get,
      email    = params.get("email").get,
      password = params.get("password").map { p => Digest.get(p) }.get,
      imgUrl   = "",
      root     = SessionHolder.root && params.get("root").isDefined
    ).id

    //update user
    redirect(url(view, "id" -> id.toString))
  }

  val self = get("/self"){ implicit dbSession =>
    val id = SessionHolder.user.get.id;

    val articleIds = Article.findAllIdBy(
      sqls.eq(Article.column.ownerId, id)
    ).grouped(pagesize).toSeq

    //show user detail
    jade("/user/view",
      "u" -> User.find(id).getOrElse(redirectFatal("/")),
      "articleIds" -> articleIds
    )
  }
  
  post("/prof_upload") { implicit dbSession =>
    val id = SessionHolder.user.get.id;
    
    fileParams.get("file") match {
      case Some(file)  =>
        //file.getContentType 
        
      case _ => BadRequest
    }
    error {
      case e: SizeConstraintExceededException => ("too much!")
    }
  }
  
  getWithoutDB("/:id/image") {
    val id = params.get("id").getOrElse(redirectFatal("/")).toLong
    
    val img = new java.io.File(FileUtil.usrImageDir, id.toString)
    
    contentType = FileUtil.getMimeType(img)
    FileUtil.getByteArray(img)
  }

}

