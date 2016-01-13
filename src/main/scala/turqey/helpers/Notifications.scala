package turqey.helpers

import turqey.entity._
import scalikejdbc._

case class StockedNotif(article: Article, user: User, id: Long)
case class CommentedNotif(article: Article, comment: ArticleComment, id: Long)

trait NotifacationHelper {

  def getStockNotifications(userId: Option[Long] = None)(implicit session: DBSession): Seq[StockedNotif] = {
    val u = User.u
    val o = User.syntax("o")
    val a = Article.a
    val sn = StockNotification.sn
    
    withSQL {
      select
        .from(StockNotification as sn)
        .join(User as u).on(sn.userId, u.id)
        .join(Article as a).on(a.id, sn.articleId)
        .join(User as o).on(a.ownerId, o.id)
        .where.not.eq(sn.read, true).and(sqls.toAndConditionOpt {
          userId.map ( sqls.eq(o.id, _) )
        })
    }.map{ rs => StockedNotif(
      article = Article(a.resultName, Some(o.resultName))(rs),
      user    = User(u.resultName)(rs),
      id      = rs.get(sn.resultName.id) )
    }.list.apply()
  }

  def getCommentNotifications(userId: Option[Long] = None)(implicit session: DBSession): Seq[CommentedNotif] = {
    val u = User.u
    val o = User.syntax("o")
    val a = Article.a
    val c = ArticleComment.ac
    val cn = CommentNotification.cn
    
    withSQL {
      select
        .from(CommentNotification as cn)
        .join(ArticleComment as c).on(c.id, cn.commentId)
        .join(User as u).on(c.userId, u.id)
        .join(Article as a).on(a.id, c.articleId)
        .join(User as o).on(a.ownerId, o.id)
        .where.not.eq(cn.read, true).and(sqls.toAndConditionOpt {
          userId.map ( sqls.eq(cn.notifyToId, _) )
        })
    }.map{ rs => CommentedNotif(
      article   = Article(a.resultName, Some(o.resultName))(rs),
      comment   = ArticleComment(c.resultName, Some(u.resultName))(rs),
      id        = rs.get(cn.resultName.id) )
    }.list.apply()
  } 

  val typesByNames = Map(
    "stock"   -> StockNotification,
    "comment" -> CommentNotification
  )

  def setNotifcationAsRead(notifType: String, ids: Seq[Long])(implicit session: DBSession): Unit = {
    import scala.language.existentials

    val typeOfNotif = typesByNames(notifType)

    withSQL {
      val col = typeOfNotif.column
      update(typeOfNotif).set(
        col.read -> true
      ).where.in(col.id, ids)
    }.update.apply()
  }
  
}
