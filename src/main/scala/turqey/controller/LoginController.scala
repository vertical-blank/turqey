package turqey.controller

import scalikejdbc._
import org.scalatra._
import turqey.html
import turqey.entity._
import turqey.servlet._

class LoginController extends ControllerBase {
  override val key = "login"
  override val shouldLoggedIn = false

  val login = get("*") {
    html.login()
  }

  post("/") {
    val id   = params.get("email")
    val pass = params.get("password")

    val digestedPass = turqey.utils.Digest.get(pass.get)

    User.findBy(sqls.eq(User.u.email, id).and.eq(User.u.password, digestedPass))
      match {
        case Some(user: User) => {
          session("user") = new UserSession(user.id, user.name, user.imgUrl)
          redirect(url(""))
        }
        case None => html.login()
      }
    
  }

}

