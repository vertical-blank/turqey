package turqey.utils

import turqey.entity._
import turqey.mail._
import scalikejdbc._

import javax.mail._
import javax.mail.internet._

object Mailer {
  case class Mail(toAddr: String, subject: String, content: String = null)

  case class StockedMail(article: Article, user: User)
  case class CommentedMail(article: Article, comment: ArticleComment)

  def sendAllNotifications()(implicit session: DBSession) = {
    val stockNotifs   = getStockNotifications()
    val commentNotifs = getCommentNotifications()

    val notifsByToAddr = (stockNotifs ++ commentNotifs) groupBy(_.toAddr)

  }

  def getStockNotifications()(implicit session: DBSession): Seq[Mail] = {
    val u = User.u
    val o = User.syntax("o")
    val a = Article.a
    val stocksAll: Seq[StockedMail] = withSQL{
      val sn = StockNotification.sn
      select
        .from(StockNotification as sn)
        .join(User as u).on(sn.userId, u.id)
        .join(Article as a).on(a.id, sn.articleId)
        .join(User as o).on(a.ownerId, o.id)
    }.map{ rs => StockedMail(
      article = Article(a.resultName, Some(o.resultName))(rs),
      user    = User(u.resultName)(rs) )
    }.list.apply()

    stocksAll.groupBy(_.article).map {
      case(article, stocks) => new Mail(
        toAddr  = article.owner.get.email,
        subject = s"${article.owner.get.name} さん 記事「${article.title}」を${stocks.size}人がストックしました",
        content = txt.stock(article, stocks).toString
      )
    }.toSeq
  }

  def getCommentNotifications()(implicit session: DBSession): Seq[Mail] = {
    val u = User.u
    val o = User.syntax("o")
    val a = Article.a
    val c = ArticleComment.ac
    val commentsAll: Seq[CommentedMail] = withSQL{
      val cn = CommentNotification.cn
      select
        .from(CommentNotification as cn)
        .join(ArticleComment as c).on(c.id, cn.commentId)
        .join(User as u).on(c.userId, u.id)
        .join(Article as a).on(a.id, c.articleId)
        .join(User as o).on(a.ownerId, o.id)
    }.map{ rs => CommentedMail(
      article   = Article(a.resultName, Some(o.resultName))(rs),
      comment   = ArticleComment(c.resultName, Some(u.resultName))(rs) )
    }.list.apply()

    commentsAll.groupBy(_.article).map {
      case(article, comments) => new Mail(
        toAddr  = article.owner.get.email,
        subject = s"${article.owner.get.name} さん 記事「${article.title}」に${comments.size}件のコメントがあります",
        content = txt.comment(article, comments).toString
      )
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
        new PasswordAuthentication("youhei.yamana@casleyconsulting.co.jp", "YB1kinjo")
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
