package turqey.controller

import org.scalatra._
import javax.servlet.http.HttpServletRequest
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.article._

import scalaz._
import scalaz.Scalaz._

import turqey.utils.Implicits._

class ArticleController extends ControllerBase  {
  override val path = "/article"

  val view = get("/:id"){
    implicit val session = AutoSession

    val articleId = params.getOrElse("id", redirect("/")).parseLong.toOption.getOrElse(redirect("/"))
    val article = Article.findBy(sqls.eq(Article.a.id, articleId)).getOrElse(redirect("/"))

    html.view(article)
  }

  val edit = get("/:id/edit"){
    implicit val session = AutoSession

    val articleId = params.getOrElse("id", redirect("/")).parseLong.toOption.getOrElse(redirect("/"))

    val article = Article.findBy(sqls.eq(Article.a.id, articleId)).getOrElse(redirect("/"))

    html.edit(Some(article))
  }

  val history = get("/:id/history"){
    implicit val session = AutoSession

    val articleId = params.getOrElse("id", redirect("/")).parseLong.toOption.getOrElse(redirect("/"))

    val article = Article.findBy(sqls.eq(Article.a.id, articleId)).getOrElse(redirect("/"))

    val histories = ArticleHistory.findAll()

    html.history(article, histories)
  }

  post("/:id"){
    implicit val session = AutoSession

    val articleId = params.getOrElse("id", "").parseLong.toOption
    val title     = params.getOrElse("title", "").toString
    val content   = params.getOrElse("content", "").toString

    articleId.foreach { id => 
      {
        val article = Article.findBy(sqls.eq(Article.a.id, articleId)).getOrElse(redirect("/"))

        val diff = turqey.utils.DiffUtil.uniDiff(article.content, content).mkString("\r\n")

        article.copy(
          title   = title,
          content = content
        ).save()

        ArticleHistory.create(
          articleId = id,
          diff      = diff,
          userId    = Some(turqey.servlet.SessionHolder.user.get.id)
        )
      }
    }
    
    redirect(url(view, "id" -> articleId.get.toString))
  }

  val newEdit = get("/edit"){
    html.edit(None)
  }

  post("/"){
    implicit val session = AutoSession

    val title     = params.getOrElse("title", "").toString
    val content   = params.getOrElse("content", "").toString

    val newId = Article.create(
      title   = title,
      content = content,
      owner   = turqey.servlet.SessionHolder.user.get.id
    ).id
    
    redirect(url(view, "id" -> newId.toString))
  }

}

