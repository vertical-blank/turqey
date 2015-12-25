package turqey.entity

import scalikejdbc._

case class TagFollowing(
  id: Long,
  userId: Long,
  followedId: Long) {

  def save()(implicit session: DBSession = TagFollowing.autoSession): TagFollowing = TagFollowing.save(this)(session)

  def destroy()(implicit session: DBSession = TagFollowing.autoSession): Unit = TagFollowing.destroy(this)(session)

}


object TagFollowing extends SQLSyntaxSupport[TagFollowing] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "TAG_FOLLOWINGS"

  override val columns = Seq("ID", "USER_ID", "FOLLOWED_ID")

  def apply(tf: SyntaxProvider[TagFollowing])(rs: WrappedResultSet): TagFollowing = apply(tf.resultName)(rs)
  def apply(tf: ResultName[TagFollowing])(rs: WrappedResultSet): TagFollowing = new TagFollowing(
    id = rs.get(tf.id),
    userId = rs.get(tf.userId),
    followedId = rs.get(tf.followedId)
  )

  val tf = TagFollowing.syntax("tf")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[TagFollowing] = {
    withSQL {
      select.from(TagFollowing as tf).where.eq(tf.id, id)
    }.map(TagFollowing(tf.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[TagFollowing] = {
    withSQL(select.from(TagFollowing as tf)).map(TagFollowing(tf.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(TagFollowing as tf)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[TagFollowing] = {
    withSQL {
      select.from(TagFollowing as tf).where.append(where)
    }.map(TagFollowing(tf.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[TagFollowing] = {
    withSQL {
      select.from(TagFollowing as tf).where.append(where)
    }.map(TagFollowing(tf.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(TagFollowing as tf).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    userId: Long,
    followedId: Long)(implicit session: DBSession = autoSession): TagFollowing = {
    val generatedKey = withSQL {
      insert.into(TagFollowing).columns(
        column.userId,
        column.followedId
      ).values(
        userId,
        followedId
      )
    }.updateAndReturnGeneratedKey.apply()

    TagFollowing(
      id = generatedKey,
      userId = userId,
      followedId = followedId)
  }

  def batchInsert(entities: Seq[TagFollowing])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'userId -> entity.userId,
        'followedId -> entity.followedId))
        SQL("""insert into TAG_FOLLOWINGS(
        USER_ID,
        FOLLOWED_ID
      ) values (
        {userId},
        {followedId}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: TagFollowing)(implicit session: DBSession = autoSession): TagFollowing = {
    withSQL {
      update(TagFollowing).set(
        column.id -> entity.id,
        column.userId -> entity.userId,
        column.followedId -> entity.followedId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: TagFollowing)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(TagFollowing).where.eq(column.id, entity.id) }.update.apply()
  }

}
