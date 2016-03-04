package turqey.controller

import org.scalatra._
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse, HttpSession }
import collection.mutable
import com.typesafe.scalalogging.{StrictLogging => Logging}

import turqey.servlet._

import scalikejdbc._

trait ControllerBase extends ScalatraServlet
  with UrlGeneratorSupport with Logging {
  
  def path: String
  
  notFound {
    serveStaticResource() getOrElse resourceNotFound()
  }
  
  def get(transformers: RouteTransformer*)(block: DBSession => Any): Route = {
    super.get(transformers:_*) {{
      DB readOnly { implicit dbSession =>
          block.apply(dbSession)
      }
    }}
  }
  def post(transformers: RouteTransformer*)(block: DBSession => Any): Route = {
    super.post(transformers:_*) {{ 
      DB localTx { implicit dbSession =>
          block.apply(dbSession)
      }
    }}
  }
  
  def getWithTx(transformers: RouteTransformer*)(block: DBSession => Any): Route = {
    super.get(transformers:_*) {{
      DB localTx { implicit dbSession =>
          block.apply(dbSession)
      }
    }}
  }
  def postWithoutTx(transformers: RouteTransformer*)(block: DBSession => Any): Route = {
    super.post(transformers:_*) {{
      DB readOnly { implicit dbSession =>
          block.apply(dbSession)
      }
    }}
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

