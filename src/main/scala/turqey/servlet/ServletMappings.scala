
package turqey.servlet

import turqey.controller._

object ServletMappings {
  val servlets: Seq[ControllerBase] = Seq(
      new AdminController("admin"),
      new ArticleController(),
      new TagController(),
      new IndexController(),
      //new LoginController(),
      new AssetsController(),
      new GoogleAuthController()
  )
  
  val byKey = this.servlets.map{ s:ControllerBase => (s.key, s) }.toMap
}

