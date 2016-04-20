package turqey.utils

import turqey.entity._
import turqey.helpers._
import turqey.mail._
import scalikejdbc._

import javax.mail._
import javax.mail.internet._

object MailNotifier extends NotifacationHelper {

  def sendAllNotifications()(implicit session: DBSession) = {
    val stockMails   = getStockMails()
    val commentMails = getCommentMails()

    MailUtil.send(stockMails ++ commentMails)
  }

  def getStockMails()(implicit session: DBSession): Seq[Mail] = {
    val notifs = getStockNotifications()

    notifs.groupBy(_.article.id).map {
      case(article, stocks) => 
        val article = stocks.head.article
        Mail(
          toAddr  = article.owner.map(o => MailAddress(o.email, Some(o.name))).get,
          subject = s"${article.owner.get.name} さん 記事「${article.title}」を${stocks.size}人がストックしました",
          content = txt.stock(article, stocks).toString
        )
    }.toSeq
  }

  def getCommentMails()(implicit session: DBSession): Seq[Mail] = {
    val notifs = getCommentNotifications()

    notifs.groupBy(_.article.id).map {
      case(article, comments) => 
        val article = comments.head.article
        Mail(
          toAddr  = article.owner.map(o => MailAddress(o.email, Some(o.name))).get,
          subject = s"${article.owner.get.name} さん 記事「${article.title}」に${comments.size}件のコメントがあります",
          content = txt.comment(article, comments).toString
        )
    }.toSeq
  }

}
