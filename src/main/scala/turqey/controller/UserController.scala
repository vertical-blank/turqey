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
    val image = params.get("image")
    val user = User.find(id).getOrElse(redirectFatal("/"))
    
    if(!user.editable){ redirectFatal("/") }

    val root = SessionHolder.root && (params.get("root").isDefined || user.self)
    
    val updUsr = user.copy(
      name     = params("name"),
      email    = params("email"),
      password = params.get("password") match {
        case Some("") | None => { user.password }
        case Some(p)  => { Digest.get(p) }
      },
      root     = root
    ).save()
    
    image.filter( _ != "" ).foreach( new FileUtil.Base64Decoder(_).saveAsUserImage(id.toString) )
    
    val sessionUsr = SessionHolder.user.get
    if (sessionUsr.id == user.id){
      session("user") = sessionUsr.copy(name = updUsr.name, email = updUsr.email)
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
    
    val image = params.get("image")
    
    val id = User.create(
      loginId  = params("loginId"),
      name     = params("name"),
      email    = params("email"),
      password = params.get("password").map { p => Digest.get(p) }.get,
      root     = SessionHolder.root && params.get("root").isDefined
    ).id
    
    image.filter( _ != "" ).foreach( new FileUtil.Base64Decoder(_).saveAsUserImage(id.toString) )

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
  
  val defaultUser = getClass.getResourceAsStream("/default_user.png")
  getWithoutDB("/:id/image") {
    val id = params.get("id").getOrElse(redirectFatal("/")).toLong
    
    val img = new java.io.File(FileUtil.usrImageDir, id.toString)
    
    contentType = "image/png"
    
    if (img.exists()) img 
    else defaultUser
  }

}

