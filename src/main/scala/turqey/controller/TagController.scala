package turqey.controller

import org.scalatra._
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.tag._

import turqey.utils.Implicits._

class TagController extends ControllerBase {
  override val path = "tag"

  val list = get("/"){
    val tags = Tag.findAllWithArticleCount()

    html.list(tags)
  }

  val view = get("/:id"){
    val tagId = params.getOrElse("id", redirect("/")).toLong
    
    val tag = Tag.find(tagId).getOrElse(redirect("/"))
    val articles = Article.findTagged(tagId)

    html.view(tag, articles)
  }

}

object TagController {
  def root: String = { turqey.servlet.ServletContextHolder.root + "/tag" }
}

