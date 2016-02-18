package turqey.controller

import org.scalatra._
import org.scalatra.scalate.ScalateSupport._
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig, SizeConstraintExceededException}
import javax.servlet.http.HttpServletRequest
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet._

class UserController extends ControllerBase with FileUploadSupport {
  configureMultipartHandling(MultipartConfig(maxFileSize = Some(3*1024*1024)))
  
  override val path = "user"
  
  val pagesize = 20

  val list = get("/") {
    jade("/user/list",
      "users" -> User.findAll())
  }

  val view = get("/:id"){
    val id = params.get("id").getOrElse(redirect("/")).toLong

    val articleIds = Article.findAllIdBy(
      sqls.eq(Article.column.ownerId, id)
    ).grouped(pagesize).toSeq

    //show user detail
    jade("/user/view",
      "u" -> User.find(id).getOrElse(redirect("/")),
      "articleIds" -> articleIds
    )
  }

  val edit = get("/:id/edit"){
    val id = params.get("id").getOrElse(redirect("/")).toLong
    val user = User.find(id).getOrElse(redirect("/"))
    
    if(!user.editable){ redirect("/") }

    jade("/user/edit", "u" -> Some(user))
  }

  val editNew = get("/edit"){
    if(!SessionHolder.root){ redirect("/") }
    
    jade("/user/edit", "u" -> None)
  }

  post("/:id"){
    val id = params.get("id").getOrElse(redirect("/")).toLong
    val user = User.find(id).getOrElse(redirect("/"))
    
    if(!user.editable){ redirect("/") }

    val updUsr = user.copy(
      name     = params.get("name").get,
      email    = params.get("email").get,
      password = params.get("password") match {
        case Some("") | None => { user.password }
        case Some(p)  => { Digest.get(p) }
      },
      imgUrl   = ""
    ).save()

    val sessionUsr = SessionHolder.user.get
    if (sessionUsr.id == user.id){
      session("user") = sessionUsr.copy(name = updUsr.name, imgUrl = updUsr.imgUrl)
    }

    redirect(url(view, "id" -> id.toString))
  }

  post("/:id/reset"){
    val id = params.get("id").getOrElse(redirect("/")).toLong
    val user = User.find(id).getOrElse(redirect("/"))
    
    if(!user.editable){ redirect("/") }

    user.copy(password = Digest.get(user.loginId) ).save()

    redirect(url(view, "id" -> id.toString))
  }

  post("/"){
    if(!SessionHolder.root){ redirect("/") }
    
    val id = User.create(
      loginId  = params.get("loginId").get,
      name     = params.get("name").get,
      email    = params.get("email").get,
      password = params.get("password").map { p => Digest.get(p) }.get,
      imgUrl   = ""
    ).id

    //update user
    redirect(url(view, "id" -> id.toString))
  }

  val self = get("/self"){
    val id = SessionHolder.user.get.id;

    val articleIds = Article.findAllIdBy(
      sqls.eq(Article.column.ownerId, id)
    ).grouped(pagesize).toSeq

    //show user detail
    jade("/user/view",
      "u" -> User.find(id).getOrElse(redirect("/")),
      "articleIds" -> articleIds
    )
  }
  
  post("/prof_upload") {
    
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

}

