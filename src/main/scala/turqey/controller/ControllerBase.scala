package turqey.controller

import org.scalatra._
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }
import collection.mutable

import turqey.servlet._

trait ControllerBase extends ScalatraServlet with UrlGeneratorSupport {
  def path: String
  
  val shouldLoggedIn = true

  def appRoot:String = ServletContextHolder.root

  /**
   * Sends a redirect response and immediately halts the current action.
  override def redirect(uri: String)(implicit request: HttpServletRequest, response: HttpServletResponse): Nothing = {
    halt(Found(fullUrl(appRoot + uri)))
  }
   */

  before() {
    SessionHolder.set(session)
    if (shouldLoggedIn && !SessionHolder.user.isDefined){
      redirect(fullUrl(appRoot + "/login", includeServletPath = false))
    }
  }
  after() {
    SessionHolder.set(session)
  }

  notFound {
    serveStaticResource() getOrElse resourceNotFound()
  }

}

