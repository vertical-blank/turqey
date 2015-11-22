package turqey.entity

import scalikejdbc._

case class ArticleSharing(
  id: Long,
  parentId: Long,
  userId: Option[Long] = None,
  groupId: Option[Long] = None) {

  def save()(implicit session: DBSession = ArticleSharing.autoSession): ArticleSharing = ArticleSharing.save(this)(session)

  def destroy()(implicit session: DBSession = ArticleSharing.autoSession): Unit = ArticleSharing.destroy(this)(session)

}


object ArticleSharing extends SQLSyntaxSupport[ArticleSharing] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "ARTICLE_SHARINGS"

  override val columns = Seq("ID", "PARENT_ID", "USER_ID", "GROUP_ID")

  def apply(as: SyntaxProvider[ArticleSharing])(rs: WrappedResultSet): ArticleSharing = apply(as.resultName)(rs)
  def apply(as: ResultName[ArticleSharing])(rs: WrappedResultSet): ArticleSharing = new ArticleSharing(
    id = rs.get(as.id),
    parentId = rs.get(as.parentId),
    userId = rs.get(as.userId),
    groupId = rs.get(as.groupId)
  )

  val as = ArticleSharing.syntax("ashr")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[ArticleSharing] = {
    withSQL {
      select.from(ArticleSharing as as).where.eq(as.id, id)
    }.map(ArticleSharing(as.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[ArticleSharing] = {
    withSQL(select.from(ArticleSharing as as)).map(ArticleSharing(as.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(ArticleSharing as as)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[ArticleSharing] = {
    withSQL {
      select.from(ArticleSharing as as).where.append(where)
    }.map(ArticleSharing(as.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ArticleSharing] = {
    withSQL {
      select.from(ArticleSharing as as).where.append(where)
    }.map(ArticleSharing(as.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(ArticleSharing as as).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    parentId: Long,
    userId: Option[Long] = None,
    groupId: Option[Long] = None)(implicit session: DBSession = autoSession): ArticleSharing = {
    val generatedKey = withSQL {
      insert.into(ArticleSharing).columns(
        column.parentId,
        column.userId,
        column.groupId
      ).values(
        parentId,
        userId,
        groupId
      )
    }.updateAndReturnGeneratedKey.apply()

    ArticleSharing(
      id = generatedKey,
      parentId = parentId,
      userId = userId,
      groupId = groupId)
  }

  def batchInsert(entities: Seq[ArticleSharing])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'parentId -> entity.parentId,
        'userId -> entity.userId,
        'groupId -> entity.groupId))
        SQL("""insert into ARTICLE_SHARINGS(
        PARENT_ID,
        USER_ID,
        GROUP_ID
      ) values (
        {parentId},
        {userId},
        {groupId}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: ArticleSharing)(implicit session: DBSession = autoSession): ArticleSharing = {
    withSQL {
      update(ArticleSharing).set(
        column.id -> entity.id,
        column.parentId -> entity.parentId,
        column.userId -> entity.userId,
        column.groupId -> entity.groupId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: ArticleSharing)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(ArticleSharing).where.eq(column.id, entity.id) }.update.apply()
  }

}
