package turqey.controller

import org.scalatra._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet._
import turqey.util._

class ApiController extends ControllerBase {
  override val path = "api"
  override val shouldLoggedIn = false

  post("/markdown") {
    contentType = "text/plain"
    val content   = params.getOrElse("content", "").toString
    Markdown.html(content);
  }
  
  case class ArticleForList(id: Long, title: String, created: String, updated: String, user: User, tags: Seq[Tag])
  
  post("/article/list") {
    contentType = "application/json"
    val ids = multiParams("id").map(_.toLong)
    
    val tagsOfArticleIds = Tag.findTagsOfArticleIds(ids)
    val lastUpdatesByIds = ArticleHistory.findLatestsByIds(ids)
    val articles = Article.findAllBy(sqls.in(Article.a.id, ids))
    val articlesWithTags = articles.map{ a => ArticleForList(
      id      = a.id,
      title   = a.title,
      created = a.created.toString("yyyy/MM/dd"),
      updated = lastUpdatesByIds.get(a.id).map(_.toString("yyyy/MM/dd")).getOrElse(""),
      user    = a.owner.get,
      tags    = tagsOfArticleIds.getOrElse(a.id, Seq()).map(_._2)
    ) }
    
    Json.toJson(tagsOfArticleIds)
  }

}

