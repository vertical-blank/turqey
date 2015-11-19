import org.scalatra._
import javax.servlet.ServletContext

import turqey._
import turqey.controller._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new Servlet, "/*")
    
    Seq(new ArticleController()).foreach { controller =>
      context.mount(controller, controller.path)
    }
  }
}
