package turqey.utils

import turqey.entity._
import scalikejdbc._

case class Mail(toAddr: String, content: String = null)

object Mailer {

  def sendAllNotifications()(implicit session: DBSession) = {
    val stockNotifs   = getStockNotifications()
    val commentNotifs = getCommentNotifications()

    //val notifsByToAddr = 
  }

  def getStockNotifications()(implicit session: DBSession): Seq[Mail] = {
    case class StockedMail(owner: User, article: Article, user: User)
    
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
      owner     = User(o.resultName)(rs),
      article   = Article(a.resultName)(rs),
      user = User(u.resultName)(rs) )
    }.list.apply()
    
    stocksAll.groupBy(_.owner).map {
      case(owner, stocks) => new Mail(
        toAddr  = owner.email,
        content = stocks.map{ x => x.user.name }.mkString(",") + "がストックしました！" 
      )
    }.toSeq
  }

  def getCommentNotifications()(implicit session: DBSession): Seq[Mail] = {
    case class CommentedMail(comment: ArticleComment, owner: User, article: Article, user: User)
    
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
      comment   = ArticleComment(c.resultName)(rs),
      owner     = User(o.resultName)(rs),
      article   = Article(a.resultName)(rs),
      user      = User(u.resultName)(rs) )
    }.list.apply()
    
    commentsAll.groupBy(_.owner).map {
      case(owner, comments) => new Mail(
        toAddr  = owner.email,
        content = comments.map{ x => x.user.name + ":" + Implicits.clobToString(x.comment.content) }.mkString(",") + "がコメントしました！" 
      )
    }.toSeq
  }

  def send(mails: Map[String, Seq[Mail]]) = {
    // connect

    // send mails

    // close

  }

}

