package turqey.controller

import org.scalatra._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet._
import turqey.helpers._

class ApiController extends ControllerBase with NotifacationHelper {
  override val path = "api"
  override val shouldLoggedIn = false
  
  before (){
    if (!SessionHolder.user.isDefined){
      halt(403, "Not Logged In.")
    }
  }

  post("/markdown") {
    contentType = "text/plain"
    val content   = params.getOrElse("content", "").toString
    Markdown.html(content);
  }
  
  post("/article/list") {
    contentType = "application/json"
    val ids = multiParams("ids").map(_.toLong)
    
    Json.toJson(Article.findForList(ids))
  }
  
  case class SiteNotif(linkTo: String, content: String)
  
  get("/notifs") {
    contentType = "application/json"
    
    val userId    = Some(SessionHolder.user.get.id)
    
    DB readOnly { implicit session => 
      val stockNotifs   = getStockNotifications(userId).groupBy(_.article).map {
        case(article, stocks) => SiteNotif(
          linkTo = turqey.servlet.ServletContextHolder.root + "/articles/" + article.id.toString,
          content = s"記事「${article.title}」を${stocks.size}人がストックしました"
        )
      }.toSeq
      
      val commentNotifs = getCommentNotifications(userId).groupBy(_.article).map {
        case(article, comments) => SiteNotif(
          linkTo = turqey.servlet.ServletContextHolder.root + "/articles/" + article.id.toString,
          content = s"記事「${article.title}」に${comments.size}件のコメントがあります"
        )
      }.toSeq
      
      val notifs = (stockNotifs ++ commentNotifs)
      
      Json.toJson(notifs)
    }

  }

}

