package turqey.controller

import org.scalatra._
import javax.servlet.http.HttpServletRequest
import collection.mutable

import turqey.servlet._

trait ControllerBase extends ScalatraServlet with UrlGeneratorSupport {

  val path:String

  before() {
    SessionHolder.set(session)
  }
  after() {
    SessionHolder.set(session)
  }

  notFound {
    serveStaticResource() getOrElse resourceNotFound()
  }

}
