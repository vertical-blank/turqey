import org.scalatra.LifeCycle
import javax.servlet.ServletContext

import turqey._
import turqey.controller._
import turqey.servlet._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    val servlets: Seq[ControllerBase] = Seq(
      new AdminController("admin"),
      new ArticleController(),
      new TagController(),
      new IndexController(),
      new AssetsController(),
      new UserController(),
      new GoogleAuthController()
    )

    servlets.foreach { controller =>
      context.mount(controller, "/" + controller.path)
    }
    println ("ScalatraBoot!")
  }
}

