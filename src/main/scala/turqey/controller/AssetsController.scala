package turqey.controller

import org.scalatra._
import turqey.html

class AssetsController extends ControllerBase {
  override val path = "assets"
  override val shouldLoggedIn = false
}

