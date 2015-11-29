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

  val list = get("/") {
    html.list(User.findAll())
  }

  val view = get("/:id"){
    val id = params.get("id").getOrElse(redirect("/")).toLong
    //show user detail
    html.view(User.find(id).getOrElse(redirect("/")))
  }

  val edit = get("/:id/edit"){
    val id = params.get("id").getOrElse(redirect("/")).toLong
    val user = User.find(id).getOrElse(redirect("/"))
    
    if(!user.editable){ redirect("/") }
    //show user detail
    html.edit(Some(user))
  }

  val editNew = get("/edit"){
    
    if(!SessionHolder.root){ redirect("/") }
    
    //show user detail
    html.edit(None)
  }

  post("/:id"){
    val id = params.get("id").getOrElse(redirect("/")).toLong
    val user = User.find(id).getOrElse(redirect("/"))
    
    if(!user.editable){ redirect("/") }

    //update user
    redirect(url(view, "id" -> id.toString))
  }

  post("/:id/reset"){
    val id = params.get("id").getOrElse(redirect("/")).toLong
    val user = User.find(id).getOrElse(redirect("/"))
    
    if(!user.editable){ redirect("/") }

    user.copy(password = Digest.get(user.loginId) ).save()

    //update user
    redirect(url(view, "id" -> id.toString))
  }

  post("/"){
    //insert user.
    
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
    //show user detail
    html.view(User.find(id).getOrElse(redirect("/")))
  }

}

