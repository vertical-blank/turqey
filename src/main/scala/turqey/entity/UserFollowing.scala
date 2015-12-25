package turqey.entity

import scalikejdbc._

case class UserFollowing(
  id: Long,
  userId: Long,
  followedId: Long) {

  def save()(implicit session: DBSession = UserFollowing.autoSession): UserFollowing = UserFollowing.save(this)(session)

  def destroy()(implicit session: DBSession = UserFollowing.autoSession): Unit = UserFollowing.destroy(this)(session)

}


object UserFollowing extends SQLSyntaxSupport[UserFollowing] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "USER_FOLLOWINGS"

  override val columns = Seq("ID", "USER_ID", "FOLLOWED_ID")

  def apply(uf: SyntaxProvider[UserFollowing])(rs: WrappedResultSet): UserFollowing = apply(uf.resultName)(rs)
  def apply(uf: ResultName[UserFollowing])(rs: WrappedResultSet): UserFollowing = new UserFollowing(
    id = rs.get(uf.id),
    userId = rs.get(uf.userId),
    followedId = rs.get(uf.followedId)
  )

  val uf = UserFollowing.syntax("uf")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[UserFollowing] = {
    withSQL {
      select.from(UserFollowing as uf).where.eq(uf.id, id)
    }.map(UserFollowing(uf.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[UserFollowing] = {
    withSQL(select.from(UserFollowing as uf)).map(UserFollowing(uf.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(UserFollowing as uf)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[UserFollowing] = {
    withSQL {
      select.from(UserFollowing as uf).where.append(where)
    }.map(UserFollowing(uf.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[UserFollowing] = {
    withSQL {
      select.from(UserFollowing as uf).where.append(where)
    }.map(UserFollowing(uf.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(UserFollowing as uf).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    userId: Long,
    followedId: Long)(implicit session: DBSession = autoSession): UserFollowing = {
    val generatedKey = withSQL {
      insert.into(UserFollowing).columns(
        column.userId,
        column.followedId
      ).values(
        userId,
        followedId
      )
    }.updateAndReturnGeneratedKey.apply()

    UserFollowing(
      id = generatedKey,
      userId = userId,
      followedId = followedId)
  }

  def batchInsert(entities: Seq[UserFollowing])(implicit session: DBSession = autoSession): Seq[Int] = {
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

  def save(entity: UserFollowing)(implicit session: DBSession = autoSession): UserFollowing = {
    withSQL {
      update(UserFollowing).set(
        column.id -> entity.id,
        column.userId -> entity.userId,
        column.followedId -> entity.followedId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: UserFollowing)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(UserFollowing).where.eq(column.id, entity.id) }.update.apply()
  }

}
