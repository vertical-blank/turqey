package turqey.entity

import scalikejdbc._

case class TagNotification(
  id: Long,
  userId: Long,
  followedId: Long) {

  def save()(implicit session: DBSession = TagNotification.autoSession): TagNotification = TagNotification.save(this)(session)

  def destroy()(implicit session: DBSession = TagNotification.autoSession): Unit = TagNotification.destroy(this)(session)

}


object TagNotification extends SQLSyntaxSupport[TagNotification] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "TAG_FOLLOWINGS"

  override val columns = Seq("ID", "USER_ID", "FOLLOWED_ID")

  def apply(tn: SyntaxProvider[TagNotification])(rs: WrappedResultSet): TagNotification = apply(tn.resultName)(rs)
  def apply(tn: ResultName[TagNotification])(rs: WrappedResultSet): TagNotification = new TagNotification(
    id = rs.get(tn.id),
    userId = rs.get(tn.userId),
    followedId = rs.get(tn.followedId)
  )

  val tn = TagNotification.syntax("tn")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[TagNotification] = {
    withSQL {
      select.from(TagNotification as tn).where.eq(tn.id, id)
    }.map(TagNotification(tn.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[TagNotification] = {
    withSQL(select.from(TagNotification as tn)).map(TagNotification(tn.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(TagNotification as tn)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[TagNotification] = {
    withSQL {
      select.from(TagNotification as tn).where.append(where)
    }.map(TagNotification(tn.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[TagNotification] = {
    withSQL {
      select.from(TagNotification as tn).where.append(where)
    }.map(TagNotification(tn.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(TagNotification as tn).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    userId: Long,
    followedId: Long)(implicit session: DBSession = autoSession): TagNotification = {
    val generatedKey = withSQL {
      insert.into(TagNotification).columns(
        column.userId,
        column.followedId
      ).values(
        userId,
        followedId
      )
    }.updateAndReturnGeneratedKey.apply()

    TagNotification(
      id = generatedKey,
      userId = userId,
      followedId = followedId)
  }

  def batchInsert(entities: Seq[TagNotification])(implicit session: DBSession = autoSession): Seq[Int] = {
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

  def save(entity: TagNotification)(implicit session: DBSession = autoSession): TagNotification = {
    withSQL {
      update(TagNotification).set(
        column.id -> entity.id,
        column.userId -> entity.userId,
        column.followedId -> entity.followedId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: TagNotification)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(TagNotification).where.eq(column.id, entity.id) }.update.apply()
  }

}
