package turqey.controller

import org.scalatra._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet._
import turqey.util._

class ApiController extends ControllerBase {
  override val path = "api"
  override val shouldLoggedIn = false

  post("/markdown") {
    contentType = "text/plain"
    val content   = params.getOrElse("content", "").toString
    Markdown.html(content);
  }

}

