package turqey.controller

import org.scalatra._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet._

class LoginController extends ControllerBase with ScalateSupport {
  override val path = "login"
  
  val login = get("*") {
    jade("/login")
  }

  post("*") {
    withTx { implicit dbSession =>
      
      logger.info(dbSession.toString)
      
      val id   = params.get("loginId")
      val pass = params.get("password")

      val digestedPass = turqey.utils.Digest.get(pass.get)

      val usr = User.findBy(sqls.eq(User.u.loginId, id).and.eq(User.u.password, digestedPass))
      usr  match {
        case Some(user: User) => {
          session("user") = new UserSession(user.id, user.name, user.imgUrl, user.root)

          user.copy(
            lastLogin = Some(new org.joda.time.DateTime())
          ).save()
          
          redirect(fullUrl("/", includeServletPath = false) + "/")
        }
        case None => jade("/login")
      }
    }
  }

}

