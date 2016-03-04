package turqey.controller

import org.scalatra._
import javax.servlet.http.HttpServletRequest
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet.SessionHolder

import turqey.utils.Implicits._

class AdminController(adminPath: String) extends AuthedController with ScalateSupport {
  override val path = adminPath

  before() {
    SessionHolder.set(session)
    val user = SessionHolder.user
    if (!user.isDefined && !user.get.root){
      redirect("")
    }
  }

  get("/") { implicit dbSession =>
    jade("/admin/index")
  }

  val systemView = get("/system") { implicit dbSession =>
  
    val settings = SystemSetting.findAll()
    val smtpSettings = SmtpSettings(settings)
  
    jade("/admin/system", "settings" -> smtpSettings)
  }
  
  post("/system") {
    redirect(url(systemView))
  }
}
