package turqey

import org.scalatra._
import scalate.ScalateSupport
import javax.servlet.http.HttpServletRequest
import collection.mutable

trait WebAppStack extends ScalatraServlet {

  notFound {
    serveStaticResource() getOrElse resourceNotFound()
  }

}
