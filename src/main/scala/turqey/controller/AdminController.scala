package turqey.controller

import org.scalatra._
import javax.servlet.http.HttpServletRequest
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet.SessionHolder

import turqey.utils.Implicits._

class AdminController extends AuthedController with ScalateSupport {
  override val path = "admin"

  before() {
    SessionHolder.set(session)
    val user = SessionHolder.user
    if (!user.isDefined && !user.get.root){
      redirectFatal("")
    }
  }

  get("/") { implicit dbSession =>
    jade("/admin/index",
    "s" -> MailUtil.setting)
  }

  post("/") { implicit dbSession =>
    redirect(url("/"))
  }
}
