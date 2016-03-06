package turqey.controller

import org.scalatra._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet._
import turqey.helpers._

case class SiteNotif(linkTo: String, content: String, notifType: String, ids: Seq[Long])

class ApiController extends AuthedController with NotifacationHelper {
  override val path = "api"
  
  before (){
    if (!SessionHolder.user.isDefined){
      halt(403, "Not Logged In.")
    }
  }

  postWithoutDB("/markdown") {
    contentType = "text/plain"
    val content   = params.getOrElse("content", "").toString
    Markdown.html(content)
  }
  
  postWithoutTx("/article/list") { implicit dbSession =>
    contentType = "application/json"
    val ids = multiParams("ids").map(_.toLong)
    
    Json.toJson(Article.findForList(ids))
  }
  
  get("/notifs") { implicit dbSession =>
    contentType = "application/json"
    
    val userId    = Some(SessionHolder.user.get.id)
     
    val stockNotifs   = getStockNotifications(userId).groupBy(_.article.id).map {
      case(id, notifs) => 
        val article = notifs.head.article
        SiteNotif(
          linkTo    = turqey.servlet.ServletContextHolder.root + "/article/" + id.toString,
          content   = s"記事「${article.title}」を${notifs.size}人がストックしました",
          notifType = "stock",
          ids       = notifs.map( _.id )
        )
    }.toSeq
    
    val commentNotifs = getCommentNotifications(userId).groupBy(_.article.id).map {
      case(id, notifs) => 
        val article = notifs.head.article
        SiteNotif(
          linkTo    = turqey.servlet.ServletContextHolder.root + "/article/" + id.toString,
          content   = s"記事「${article.title}」に${notifs.size}件のコメントがあります",
          notifType = "comment",
          ids       = notifs.map( _.id )
        )
    }.toSeq
    
    val articleNotifs = getArticleNotifications(userId)
    
    val articleUpdateNotifs = articleNotifs
      .filter( _.notifType == ArticleNotification.TYPES.UPDATE )
      .groupBy(_.article.id).map {
      case(id, notifs) => 
        val article = notifs.head.article
        SiteNotif(
          linkTo    = turqey.servlet.ServletContextHolder.root + "/article/" + id.toString,
          content   = s"記事「${article.title}」が更新されました",
          notifType = "article",
          ids       = notifs.map( _.id )
        )
    }.toSeq
    
    val articleCreateNotifs = articleNotifs
      .filter( _.notifType == ArticleNotification.TYPES.CREATE ).map {
        notif => 
        val article = notif.article
        SiteNotif(
          linkTo    = turqey.servlet.ServletContextHolder.root + "/article/" + article.id.toString,
          content   = s"記事「${article.title}」が投稿されました",
          notifType = "article",
          ids       = Seq(notif.id)
        )
    }.toSeq
    
    val notifs = (stockNotifs ++ commentNotifs ++ articleUpdateNotifs ++ articleCreateNotifs)
    Json.toJson(notifs)

  }
  
  post("/notif_read") { implicit dbSession =>
    contentType = "application/json"
    val ids       = multiParams("ids").map(_.toLong)
    val notifType = params("notifType")

    setNotifcationAsRead(notifType, ids)
  }

}

