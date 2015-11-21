package turqey.controller

import scalikejdbc._
import org.scalatra._
import turqey.html
import turqey.entity._

class LoginController extends ControllerBase  {
  override val path = "/login"
  override val shouldLoggedIn = false

  val login = get("/") {
    html.login()
  }

  post("/") {
    val id   = params.get("id")
    val pass = params.get("pass")

    val digestedPass = turqey.utils.Digest.get(pass.get)

    val user = User.findBy(sqls.eq(User.u.email, id).and.eq(User.u.password, pass))

  }

}

