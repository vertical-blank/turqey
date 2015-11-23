package turqey.controller

import org.scalatra._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet._
import turqey.html

class IndexController extends ControllerBase  {
  override val path = "/"

  get("/") {
    val articles = 
    if (SessionHolder.user isDefined){
      Article.findAll()
    }
    else {
      redirect(url("/login"))
    }

    val stocks = ArticleStock.findAllBy(sqls.eq(ArticleStock.column.userId, SessionHolder.user.get.id)).map( x => x.articleId )

    html.index(articles, stocks)
  }

}

