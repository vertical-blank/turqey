import org.scalatra._
import javax.servlet.ServletContext

import turqey._
import turqey.controller._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    Seq(
      new ArticleController(),
      new IndexController()
    ).foreach { controller =>
      context.mount(controller, controller.path)
    }
  }
}
