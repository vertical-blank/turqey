package turqey.controller

import org.scalatra._
import turqey.html

class LoginController extends ControllerBase  {
  override val path = "/login"
  override val shouldLoggedIn = false

  val login = get("/") {
    html.login()
  }

}

