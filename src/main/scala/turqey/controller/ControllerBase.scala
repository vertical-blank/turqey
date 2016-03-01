package turqey.controller

import org.scalatra._
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }
import collection.mutable

import turqey.servlet._

trait ControllerBase extends ScalatraServlet
  with UrlGeneratorSupport {
  
  def path: String
  
  notFound {
    serveStaticResource() getOrElse resourceNotFound()
  }
}

trait AuthedController extends ControllerBase {
  def appRoot:String = ServletContextHolder.root

  before() {
    SessionHolder.set(session)
    if (!SessionHolder.user.isDefined){
      redirect(fullUrl(appRoot + "/login", includeServletPath = false))
    }
  }
  
  after() {
    SessionHolder.set(session)
  }
}

trait ScalateSupport extends org.scalatra.scalate.ScalateSupport {
  override var contentType = "text/html"
}

