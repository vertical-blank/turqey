package turqey.helpers

import turqey.entity._
import scalikejdbc._

case class StockedNotif(article: Article, user: User)
case class CommentedNotif(article: Article, comment: ArticleComment)

trait NotifacationHelper {

  def getStockNotifications(userId: Option[Long] = None)(implicit session: DBSession): Seq[StockedNotif] = {
    val u = User.u
    val o = User.syntax("o")
    val a = Article.a
    
    withSQL {
      val sn = StockNotification.sn
      select
        .from(StockNotification as sn)
        .join(User as u).on(sn.userId, u.id)
        .join(Article as a).on(a.id, sn.articleId)
        .join(User as o).on(a.ownerId, o.id)
        .where(sqls.toAndConditionOpt {
          userId.map { u => sqls.eq(o.id, userId) }
        })
    }.map{ rs => StockedNotif(
      article = Article(a.resultName, Some(o.resultName))(rs),
      user    = User(u.resultName)(rs) )
    }.list.apply()
  }

  def getCommentNotifications(userId: Option[Long] = None)(implicit session: DBSession): Seq[CommentedNotif] = {
    val u = User.u
    val o = User.syntax("o")
    val a = Article.a
    val c = ArticleComment.ac
    
    withSQL {
      val cn = CommentNotification.cn
      select
        .from(CommentNotification as cn)
        .join(ArticleComment as c).on(c.id, cn.commentId)
        .join(User as u).on(c.userId, u.id)
        .join(Article as a).on(a.id, c.articleId)
        .join(User as o).on(a.ownerId, o.id)
        .where(sqls.toAndConditionOpt {
          userId.map { u => sqls.eq(o.id, userId) }
        })
    }.map{ rs => CommentedNotif(
      article   = Article(a.resultName, Some(o.resultName))(rs),
      comment   = ArticleComment(c.resultName, Some(u.resultName))(rs) )
    }.list.apply()
  }  
  
}
