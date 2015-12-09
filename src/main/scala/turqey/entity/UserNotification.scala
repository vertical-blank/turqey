package turqey.entity

import scalikejdbc._

case class UserNotification(
  id: Long,
  userId: Long,
  followedId: Long) {

  def save()(implicit session: DBSession = UserNotification.autoSession): UserNotification = UserNotification.save(this)(session)

  def destroy()(implicit session: DBSession = UserNotification.autoSession): Unit = UserNotification.destroy(this)(session)

}


object UserNotification extends SQLSyntaxSupport[UserNotification] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "USER_FOLLOWINGS"

  override val columns = Seq("ID", "USER_ID", "FOLLOWED_ID")

  def apply(un: SyntaxProvider[UserNotification])(rs: WrappedResultSet): UserNotification = apply(un.resultName)(rs)
  def apply(un: ResultName[UserNotification])(rs: WrappedResultSet): UserNotification = new UserNotification(
    id = rs.get(un.id),
    userId = rs.get(un.userId),
    followedId = rs.get(un.followedId)
  )

  val un = UserNotification.syntax("un")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[UserNotification] = {
    withSQL {
      select.from(UserNotification as un).where.eq(un.id, id)
    }.map(UserNotification(un.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[UserNotification] = {
    withSQL(select.from(UserNotification as un)).map(UserNotification(un.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(UserNotification as un)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[UserNotification] = {
    withSQL {
      select.from(UserNotification as un).where.append(where)
    }.map(UserNotification(un.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[UserNotification] = {
    withSQL {
      select.from(UserNotification as un).where.append(where)
    }.map(UserNotification(un.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(UserNotification as un).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    userId: Long,
    followedId: Long)(implicit session: DBSession = autoSession): UserNotification = {
    val generatedKey = withSQL {
      insert.into(UserNotification).columns(
        column.userId,
        column.followedId
      ).values(
        userId,
        followedId
      )
    }.updateAndReturnGeneratedKey.apply()

    UserNotification(
      id = generatedKey,
      userId = userId,
      followedId = followedId)
  }

  def batchInsert(entities: Seq[UserNotification])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'userId -> entity.userId,
        'followedId -> entity.followedId))
        SQL("""insert into USER_FOLLOWINGS(
        USER_ID,
        FOLLOWED_ID
      ) values (
        {userId},
        {followedId}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: UserNotification)(implicit session: DBSession = autoSession): UserNotification = {
    withSQL {
      update(UserNotification).set(
        column.id -> entity.id,
        column.userId -> entity.userId,
        column.followedId -> entity.followedId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: UserNotification)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(UserNotification).where.eq(column.id, entity.id) }.update.apply()
  }

}
