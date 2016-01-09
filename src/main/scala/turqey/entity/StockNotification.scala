package turqey.entity

import scalikejdbc._
import org.joda.time.{DateTime}

case class StockNotification(
  id: Long,
  articleId: Long,
  userId: Long,
  read: Boolean = false,
  created: DateTime = null) {

  def save()(implicit session: DBSession = StockNotification.autoSession): StockNotification = StockNotification.save(this)(session)

  def destroy()(implicit session: DBSession = StockNotification.autoSession): Unit = StockNotification.destroy(this)(session)

}


object StockNotification extends SQLSyntaxSupport[StockNotification] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "STOCK_NOTIFICATIONS"

  override val columns = Seq("ID", "ARTICLE_ID", "USER_ID", "READ", "CREATED")

  def apply(sn: SyntaxProvider[StockNotification])(rs: WrappedResultSet): StockNotification = apply(sn.resultName)(rs)
  def apply(sn: ResultName[StockNotification])(rs: WrappedResultSet): StockNotification = new StockNotification(
    id = rs.get(sn.id),
    articleId = rs.get(sn.articleId),
    userId = rs.get(sn.userId),
    read = rs.get(sn.read),
    created = rs.get(sn.created)
  )

  val sn = StockNotification.syntax("sn")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[StockNotification] = {
    withSQL {
      select.from(StockNotification as sn).where.eq(sn.id, id)
    }.map(StockNotification(sn.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[StockNotification] = {
    withSQL(select.from(StockNotification as sn)).map(StockNotification(sn.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(StockNotification as sn)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[StockNotification] = {
    withSQL {
      select.from(StockNotification as sn).where.append(where)
    }.map(StockNotification(sn.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[StockNotification] = {
    withSQL {
      select.from(StockNotification as sn).where.append(where)
    }.map(StockNotification(sn.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(StockNotification as sn).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    articleId: Long,
    userId: Long)(implicit session: DBSession = autoSession): StockNotification = {
    val generatedKey = withSQL {
      insert.into(StockNotification).columns(
        column.articleId,
        column.userId
      ).values(
        articleId,
        userId
      )
    }.updateAndReturnGeneratedKey.apply()

    StockNotification(
      id = generatedKey,
      articleId = articleId,
      userId = userId)
  }

  def batchInsert(entities: Seq[StockNotification])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'articleId -> entity.articleId,
        'userId -> entity.userId))
        SQL("""insert into STOCK_NOTIFICATIONS(
        ARTICLE_ID,
        USER_ID
      ) values (
        {articleId},
        {userId}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: StockNotification)(implicit session: DBSession = autoSession): StockNotification = {
    withSQL {
      update(StockNotification).set(
        column.id -> entity.id,
        column.articleId -> entity.articleId,
        column.userId -> entity.userId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: StockNotification)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(StockNotification).where.eq(column.id, entity.id) }.update.apply()
  }

}
