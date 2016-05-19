package turqey.utils

import turqey.entity._
import turqey.helpers._
import turqey.mail.txt
import scalikejdbc._

import javax.mail._
import javax.mail.internet._

object Mailer extends NotifacationHelper {
  case class Mail(toAddr: String, subject: String, content: String = null)

  def sendAllNotifications()(implicit session: DBSession) = {
    val stockMails   = getStockMails()
    val commentMails = getCommentMails()

    val notifsByToAddr = (stockMails ++ commentMails) groupBy(_.toAddr)
  }

  def getStockMails()(implicit session: DBSession): Seq[Mail] = {
    val notifs = getStockNotifications()

    notifs.groupBy(_.article.id).flatMap {
      case(article, stocks) => 
        val article = stocks.head.article
        article.owner.map ( o =>
          new Mail(
            toAddr  = o.email,
            subject = s"${o.name} さん 記事「${article.title}」を${stocks.size}人がストックしました",
            content = txt.stock(article, stocks).toString
          ))
    }.toSeq
  }

  def getCommentMails()(implicit session: DBSession): Seq[Mail] = {
    val notifs = getCommentNotifications()

    notifs.groupBy(_.article.id).flatMap {
      case(article, comments) => 
        val article = comments.head.article
        article.owner.map ( o =>
          new Mail(
            toAddr  = o.email,
            subject = s"${o.name} さん 記事「${article.title}」に${comments.size}件のコメントがあります",
            content = txt.comment(article, comments).toString
          ))
    }.toSeq
  }

  def connect(): Session = {
    val property = new java.util.Properties()
    property.put("mail.smtp.host",            "smtp.gmail.com")
    property.put("mail.smtp.port",            "587")
    property.put("mail.smtp.auth",            "true")
    property.put("mail.smtp.starttls.enable", "true")

    Session.getInstance(property, new javax.mail.Authenticator(){
      override def getPasswordAuthentication(): PasswordAuthentication = {
        new PasswordAuthentication("noreply@casleyconsulting.co.jp", "")
      }
    })
  }

  def send(mailsByToAddr: Map[String, Seq[Mail]]) = {
    val session = this.connect()

    mailsByToAddr.foreach { case (toAddr, mails) =>

      mails.foreach { m =>
        val mimeMessage = new MimeMessage(session)

        mimeMessage.setFrom(new InternetAddress("noreply@casleyconsulting.co.jp", "Turqey@Casley"))
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddr))
        mimeMessage.setSubject(m.subject, "ISO-2022-JP");
        mimeMessage.setText(m.content, "ISO-2022-JP");

        Transport.send(mimeMessage)
      }
    }
  }

}
