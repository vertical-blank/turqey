package turqey.entity

import scalikejdbc._
import org.joda.time.{DateTime}

case class ArticleNotification(
  id: Long,
  articleId: Long,
  read: Boolean = false,
  created: DateTime = null,
  notifyToId: Long,
  notifyType: Int
  ) {

  def save()(implicit session: DBSession = ArticleNotification.autoSession): ArticleNotification = ArticleNotification.save(this)(session)

  def destroy()(implicit session: DBSession = ArticleNotification.autoSession): Unit = ArticleNotification.destroy(this)(session)

}


object ArticleNotification extends SQLSyntaxSupport[ArticleNotification] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "ARTICLE_NOTIFICATIONS"

  override val columns = Seq("ID", "ARTICLE_ID", "READ", "CREATED", "NOTIFY_TO_ID", "NOTIFY_TYPE")
  
  object TYPES {
    val CREATE = 1
    val UPDATE = 2
  }

  def apply(an: SyntaxProvider[ArticleNotification])(rs: WrappedResultSet): ArticleNotification = apply(an.resultName)(rs)
  def apply(an: ResultName[ArticleNotification])(rs: WrappedResultSet): ArticleNotification = new ArticleNotification(
    id = rs.get(an.id),
    articleId = rs.get(an.articleId),
    read = rs.get(an.read),
    created = rs.get(an.created),
    notifyToId = rs.get(an.notifyToId),
    notifyType = rs.get(an.notifyType)
  )

  val an = ArticleNotification.syntax("an")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[ArticleNotification] = {
    withSQL {
      select.from(ArticleNotification as an).where.eq(an.id, id)
    }.map(ArticleNotification(an.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[ArticleNotification] = {
    withSQL(select.from(ArticleNotification as an)).map(ArticleNotification(an.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(ArticleNotification as an)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[ArticleNotification] = {
    withSQL {
      select.from(ArticleNotification as an).where.append(where)
    }.map(ArticleNotification(an.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ArticleNotification] = {
    withSQL {
      select.from(ArticleNotification as an).where.append(where)
    }.map(ArticleNotification(an.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(ArticleNotification as an).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    articleId: Long,
    notifyToId: Long,
    notifyType: Int)(implicit session: DBSession = autoSession): ArticleNotification = {
    val generatedKey = withSQL {
      insert.into(ArticleNotification).columns(
        column.articleId,
        column.notifyToId,
        column.notifyType
      ).values(
        articleId,
        notifyToId,
        notifyType
      )
    }.updateAndReturnGeneratedKey.apply()

    ArticleNotification(
      id = generatedKey,
      articleId = articleId,
      notifyToId = notifyToId,
      notifyType = notifyType
      )
  }

  def batchInsert(entities: Seq[ArticleNotification])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'articleId -> entity.articleId))
        SQL("""insert into ARTICLE_NOTIFICATIONS(
        ARTICLE_ID
      ) values (
        {articleId}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: ArticleNotification)(implicit session: DBSession = autoSession): ArticleNotification = {
    withSQL {
      update(ArticleNotification).set(
        column.id -> entity.id,
        column.articleId -> entity.articleId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: ArticleNotification)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(ArticleNotification).where.eq(column.id, entity.id) }.update.apply()
  }

}
