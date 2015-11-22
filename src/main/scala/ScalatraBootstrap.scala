import org.scalatra._
import javax.servlet.ServletContext

import turqey._
import turqey.controller._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    Seq(
      new AdminController("admin"),
      new ArticleController(),
      new TagController(),
      new IndexController(),
      new LoginController(),
      new AssetsController(),
      new GoogleAuthController()
    ).foreach { controller =>
      context.mount(controller, controller.path)
    }
  }
}
