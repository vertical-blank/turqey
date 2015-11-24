package turqey.controller

import org.scalatra._
import javax.servlet.http.HttpServletRequest
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.article._

import turqey.utils.Implicits._

class AdminController(adminPath: String) extends ControllerBase {
  override val key = adminPath

  get("/"){
    <html>
      <body>
        ADMIN CONSOLE!!!
      </body>
    </html>
  }

}

