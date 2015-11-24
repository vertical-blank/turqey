import org.scalatra.LifeCycle
import javax.servlet.ServletContext

import turqey._
import turqey.controller._
import turqey.servlet._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    ServletMappings.servlets.foreach { controller =>
      context.mount(controller, controller.path)
    }
  }
}

