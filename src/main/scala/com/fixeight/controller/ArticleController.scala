package com.fixeight.controller

import org.scalatra._
import javax.servlet.http.HttpServletRequest
import io.github.gitbucket.markedj._
import scalikejdbc._

import com.fixeight.entity._
import com.fixeight.utils._
import com.fixeight.article._

import scalaz._
import scalaz.Scalaz._

import com.fixeight.utils.Implicits._

class ArticleController extends ControllerBase  {
  override val path = "/article"

  val view = get("/:id"){
    implicit val session = AutoSession

    val articleId = params.getOrElse("id", redirect("/")).parseLong.toOption.getOrElse(redirect("/"))

    val a = Article.syntax("a")
    val article = withSQL {
      select.from(Article as a).where.eq(a.id, articleId)
    }.map(rs => Article(a)(rs)).single.apply.getOrElse(redirect("/"))

    html.view(article)
  }

  val edit = get("/:id/edit"){
    implicit val session = AutoSession

    val articleId = params.getOrElse("id", redirect("/")).parseLong.toOption.getOrElse(redirect("/"))

    val a = Article.syntax("a")
    val article = withSQL {
      select.from(Article as a).where.eq(a.id, articleId)
    }.map(rs => Article(a)(rs)).single.apply.getOrElse(redirect("/"))

    html.edit(Some(article))
  }

  val history = get("/:id/history"){
    implicit val session = AutoSession

    val articleId = params.getOrElse("id", redirect("/")).parseLong.toOption.getOrElse(redirect("/"))

    val a = Article.syntax("a")
    val article = withSQL {
      select.from(Article as a).where.eq(a.id, articleId)
    }.map(rs => Article(a)(rs)).single.apply.getOrElse(redirect("/"))

    val h = ArticleHistory.syntax("h")
    val histories = withSQL {
      select.from(ArticleHistory as h).where.eq(h.articleId, articleId)
    }.map(rs => ArticleHistory(h)(rs)).list.apply

    html.history(article, histories)
  }

  post("/:id"){
    implicit val session = AutoSession

    val articleId = params.getOrElse("id", "").parseLong.toOption
    val title     = params.getOrElse("title", "").toString
    val content   = params.getOrElse("content", "").toString

    articleId.foreach { id => 
      {
        val a = Article.syntax("a")
        val article = withSQL {
          select.from(Article as a).where.eq(a.id, id)
        }.map(rs => Article(a)(rs)).single.apply.get

        val diff = com.fixeight.utils.DiffUtil.uniDiff(article.content, content).mkString("\r\n")

        withSQL {
          update(Article).set(
            Article.column.title   -> title,
            Article.column.content -> content
          ).where.eq(Article.column.id, id)
        }.update.apply

        withSQL {
          insert.into(ArticleHistory).namedValues(
            ArticleHistory.column.articleId -> id,
            ArticleHistory.column.diff      -> diff,
            ArticleHistory.column.userId    -> 1
          )
        }.update.apply
      }
    }
    
    redirect(url(view, "id" -> articleId.get.toString))
  }

  val newEdit = get("/edit"){
    implicit val session = AutoSession

    html.edit(None)
  }

  post("/"){
    implicit val session = AutoSession

    val title     = params.getOrElse("title", "").toString
    val content   = params.getOrElse("content", "").toString

    val newId = withSQL {
      insert.into(Article).namedValues(
        Article.column.title   -> title,
        Article.column.content -> content,
        Article.column.owner   -> 1
      )
    }.updateAndReturnGeneratedKey.apply
    
    redirect(url(view, "id" -> newId.toString))
  }

}

