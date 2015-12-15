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
  
  post("/article/list") {
    contentType = "application/json"
    val ids = multiParams("id").map(_.toLong)
    
    Json.toJson(Article.findForList(ids))
  }

}

