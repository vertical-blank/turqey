package turqey.controller

import org.scalatra._
import javax.servlet.http.HttpServletRequest
import collection.mutable

import turqey.servlet._

trait ControllerBase extends ScalatraServlet with UrlGeneratorSupport {

  val path: String
  val shouldLoggedIn = true

  before() {
    SessionHolder.set(session)
    if (shouldLoggedIn && !SessionHolder.user.isDefined){
      redirect("/login")
    }
  }
  after() {
    SessionHolder.set(session)
  }

  notFound {
    serveStaticResource() getOrElse resourceNotFound()
  }

}
