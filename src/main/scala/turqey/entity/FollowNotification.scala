package turqey.entity

import scalikejdbc._
import org.joda.time.{DateTime}

case class FollowNotification(
  id: Long,
  userId: Long,
  read: Option[Boolean] = None,
  created: DateTime = null) {

  def save()(implicit session: DBSession = FollowNotification.autoSession): FollowNotification = FollowNotification.save(this)(session)

  def destroy()(implicit session: DBSession = FollowNotification.autoSession): Unit = FollowNotification.destroy(this)(session)

}


object FollowNotification extends SQLSyntaxSupport[FollowNotification] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "FOLLOW_NOTIFICATIONS"

  override val columns = Seq("ID", "USER_ID", "READ", "CREATED")

  def apply(fn: SyntaxProvider[FollowNotification])(rs: WrappedResultSet): FollowNotification = apply(fn.resultName)(rs)
  def apply(fn: ResultName[FollowNotification])(rs: WrappedResultSet): FollowNotification = new FollowNotification(
    id = rs.get(fn.id),
    userId = rs.get(fn.userId),
    read = rs.get(fn.read),
    created = rs.get(fn.created)
  )

  val fn = FollowNotification.syntax("fn")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[FollowNotification] = {
    withSQL {
      select.from(FollowNotification as fn).where.eq(fn.id, id)
    }.map(FollowNotification(fn.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[FollowNotification] = {
    withSQL(select.from(FollowNotification as fn)).map(FollowNotification(fn.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(FollowNotification as fn)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[FollowNotification] = {
    withSQL {
      select.from(FollowNotification as fn).where.append(where)
    }.map(FollowNotification(fn.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[FollowNotification] = {
    withSQL {
      select.from(FollowNotification as fn).where.append(where)
    }.map(FollowNotification(fn.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(FollowNotification as fn).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    userId: Long)(implicit session: DBSession = autoSession): FollowNotification = {
    val generatedKey = withSQL {
      insert.into(FollowNotification).columns(
        column.userId
      ).values(
        userId
      )
    }.updateAndReturnGeneratedKey.apply()

    FollowNotification(
      id = generatedKey,
      userId = userId)
  }

  def batchInsert(entities: Seq[FollowNotification])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'userId -> entity.userId))
        SQL("""insert into FOLLOW_NOTIFICATIONS(
        USER_ID
      ) values (
        {userId}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: FollowNotification)(implicit session: DBSession = autoSession): FollowNotification = {
    withSQL {
      update(FollowNotification).set(
        column.id -> entity.id,
        column.userId -> entity.userId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: FollowNotification)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(FollowNotification).where.eq(column.id, entity.id) }.update.apply()
  }

}
