
import org.scalatra._
import javax.servlet.ServletContext

import turqey._
import turqey.controller._
import turqey.servlet._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    val servlets: Seq[ControllerBase] = Seq(
      new AdminController("admin"),
      new ApiController(),
      new ArticleController(),
      new TagController(),
      new IndexController(),
      //new AssetsController(),
      new UserController()
    )

    servlets.foreach { controller =>
      context.mount(controller, "/" + controller.path)
    }
    
    context.mount(new AssetsController(), "/assets")
    
  }
}

