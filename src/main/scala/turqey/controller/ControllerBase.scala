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
      try {
        DB localTx { implicit dbSession =>
            block.apply(dbSession)
        }
      } catch {
        case e: SuccessfulRedirectException => redirectFatal(e)
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
  
  import java.lang.{ Integer => JInteger }
  import scala.util.control.ControlThrowable
  import scala.util.control.NoStackTrace
  
  
  def redirectFatal(uri: String)(implicit request: HttpServletRequest, response: HttpServletResponse): Nothing = {
    super.redirect(uri)
  }
  
  override def redirect(uri: String)(implicit request: HttpServletRequest, response: HttpServletResponse): Nothing = {
    throw new SuccessfulRedirectException(uri: String)
  }
  
  private[turqey] case class SuccessfulRedirectException(uri: String) extends ControlThrowable with NoStackTrace
  
  def redirectFatal(e: SuccessfulRedirectException)(implicit request: HttpServletRequest, response: HttpServletResponse): Nothing = {
    super.redirect(e.uri)
  }

}

trait AuthedController extends ControllerBase {
  def appRoot:String = ServletContextHolder.root

  before() {
    SessionHolder.set(session)
    if (!SessionHolder.user.isDefined){
      redirectFatal(fullUrl(appRoot + "/login/", includeServletPath = false))
    }
  }
  
  after() {
    SessionHolder.set(session)
  }
}

trait ScalateSupport extends org.scalatra.scalate.ScalateSupport {
  override var contentType = "text/html"
}

