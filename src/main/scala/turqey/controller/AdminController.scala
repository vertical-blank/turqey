package turqey.controller

import org.scalatra._
import javax.servlet.http.HttpServletRequest
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.admin._
import turqey.servlet.SessionHolder
import turqey.user

import turqey.utils.Implicits._

class AdminController(adminPath: String) extends ControllerBase {
  override val key = adminPath
  
  before() {
    SessionHolder.set(session)
    val user = SessionHolder.user
    if (shouldLoggedIn && !user.isDefined && !user.get.root){
      redirect("")
    }
  }

  get("/"){
    html.index()
  }

  get("/user"){
    // list all users.
    user.html.list(User.findAll())
  }

  val view = get("/user/:id"){
    val id = params.get("id").getOrElse(redirect("/")).toLong
    //show user detail
    user.html.view(User.find(id).getOrElse(redirect("/")))
  }

  get("/user/edit"){
    //edit new user.
    user.html.edit(None)
  }

  post("/user"){
    //insert user.
    
    val id = User.create(
      name   = "",
      email  = "",
      imgUrl = ""
    ).id

    redirect(url(view, "id" -> id.toString))
  }

  get("/user/:id/edit"){
    val id = params.get("id").getOrElse(redirect("/")).toLong
    //show user detail
    user.html.edit(Some(User.find(id).getOrElse(redirect("/"))))
  }

  post("/user/:id"){
    val id = params.get("id").getOrElse(redirect("/")).toLong
    val user = User.find(id).getOrElse(redirect("/"))
    //update user
    redirect(url(view, "id" -> id.toString))
  }

}
