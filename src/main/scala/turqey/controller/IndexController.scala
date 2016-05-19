package turqey.controller

import org.scalatra._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet._

class IndexController extends AuthedController with ScalateSupport {
  override val path = ""
  
  val pagesize = 20

  val entry = get("/") { implicit dbSession =>

    val articleIds = Article.findAllId().grouped(pagesize).toSeq
    val stockIds = ArticleStock.findAllBy(
      sqls.eq(ArticleStock.column.userId, user.id)
    ).map( x => x.articleId ).grouped(pagesize).toSeq
    val ownIds = Article.findAllIdBy(
      sqls.eq(Article.column.ownerId, user.id)
    ).grouped(pagesize).toSeq
    val commentedIds = ArticleComment.findAllBy(
      sqls.eq(ArticleStock.column.userId, user.id)
    ).map( x => x.articleId ).grouped(pagesize).toSeq
    val followingIds = ArticleTagging.followingArticleIds(user.id).grouped(pagesize).toSeq

    jade("/index", 
      "articleIds" -> articleIds,
      "stockIds" -> stockIds,
      "ownIds" -> ownIds,
      "commentedIds" -> commentedIds,
      "followingIds" -> followingIds)
  }

  val logout = get("/logout") { implicit dbSession =>
    session.invalidate()
    
    redirect(url("/login/"))
  }

}

