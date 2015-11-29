package turqey.controller

import org.scalatra._
import javax.servlet.http.HttpServletRequest
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.article._

class UserController extends ControllerBase {
  override val path = "user"

  val view = get("/:id"){
    
  }

  val edit = get("/:id/edit"){

  }

  val self = get("/self"){
    val selfId = turqey.servlet.SessionHolder.user.get.id;


  }

}

