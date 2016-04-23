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
    "smtp" -> MailUtil.settingOpt)
  }

  post("/") { implicit dbSession =>
    MailUtil.saveSetting(SmtpSetting(
      host     =  params.get("host").filter(!_.isEmpty),
      port     =  params.get("port").map(_.toInt),
      authId   =  params.get("authId").filter(!_.isEmpty),
      authPass =  params.get("authPass").filter(!_.isEmpty),
      ssl      =  params.get("tls").map(_ == "on"),
      from     =  MailAddress(
          params.getOrElse("fromAddr", ""),
          params.get("fromName").filter(!_.isEmpty)
        )
    ))
    redirect(url("/"))
  }
}
