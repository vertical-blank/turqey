package turqey.controller

import org.scalatra._
import javax.servlet.http.HttpServletRequest
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.user._
import turqey.servlet._

class UserController extends ControllerBase {
  override val path = "user"
  
  val pagesize = 20

  val list = get("/") {
    html.list(User.findAll())
  }

  val view = get("/:id"){
    val id = params.get("id").getOrElse(redirect("/")).toLong

    val articleIds = Article.findAllIdBy(
      sqls.eq(Article.column.ownerId, id)
    ).grouped(pagesize).toSeq

    //show user detail
    html.view(User.find(id).getOrElse(redirect("/")), articleIds)
  }

  val edit = get("/:id/edit"){
    val id = params.get("id").getOrElse(redirect("/")).toLong
    val user = User.find(id).getOrElse(redirect("/"))
    
    if(!user.editable){ redirect("/") }

    html.edit(Some(user))
  }

  val editNew = get("/edit"){
    if(!SessionHolder.root){ redirect("/") }
    
    html.edit(None)
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
    html.view(User.find(id).getOrElse(redirect("/")), articleIds)
  }

}

