package turqey.controller

import org.scalatra._
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse, HttpSession }
import collection.mutable
import com.typesafe.scalalogging.StrictLogging

import turqey.servlet._

import scalikejdbc._

trait ControllerBase extends ScalatraServlet
  with UrlGeneratorSupport with StrictLogging {
  
  def path: String
  
  notFound {
    serveStaticResource() getOrElse resourceNotFound()
  }
  
  def readOnly(block: DBSession => Any) = {
    DB readOnly block
  }
  def withTx(block: DBSession => Any) = {
    DB localTx block
  }
  
}

trait AuthedController extends ControllerBase {
  def appRoot:String = ServletContextHolder.root

  before() {
    SessionHolder.set(session)
    if (!SessionHolder.user.isDefined){
      redirect(fullUrl(appRoot + "/login/", includeServletPath = false))
    }
  }
  
  after() {
    SessionHolder.set(session)
  }
}

trait ScalateSupport extends org.scalatra.scalate.ScalateSupport {
  override var contentType = "text/html"
}

