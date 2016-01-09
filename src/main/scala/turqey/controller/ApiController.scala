package turqey.controller

import org.scalatra._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet._
import turqey.helpers._

case class SiteNotif(linkTo: String, content: String, notifType: String, ids: Seq[Long])

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
  
  get("/notifs") {
    contentType = "application/json"
    
    val userId    = Some(SessionHolder.user.get.id)
    
    DB readOnly { implicit session => 
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
      
      val notifs = (stockNotifs ++ commentNotifs)
      Json.toJson(notifs)
    }

  }
  
  post("/notif_read") {
    contentType = "application/json"
    val ids       = multiParams("ids").map(_.toLong)
    val notifType = params("notifType")

    DB localTx { implicit session =>
      setNotifcationAsRead(notifType, ids)
    }
  }

}

