package turqey.entity

import scalikejdbc._
import org.joda.time.{DateTime}

case class CommentNotification(
  id: Long,
  commentId: Long,
  read: Boolean = false, 
  created: DateTime = null) {

  def save()(implicit session: DBSession = CommentNotification.autoSession): CommentNotification = CommentNotification.save(this)(session)

  def destroy()(implicit session: DBSession = CommentNotification.autoSession): Unit = CommentNotification.destroy(this)(session)

}


object CommentNotification extends SQLSyntaxSupport[CommentNotification] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "COMMENT_NOTIFICATIONS"

  override val columns = Seq("ID", "COMMENT_ID", "READ", "CREATED")

  def apply(cn: SyntaxProvider[CommentNotification])(rs: WrappedResultSet): CommentNotification = apply(cn.resultName)(rs)
  def apply(cn: ResultName[CommentNotification])(rs: WrappedResultSet): CommentNotification = new CommentNotification(
    id = rs.get(cn.id),
    commentId = rs.get(cn.commentId),
    read = rs.get(cn.read),
    created = rs.get(cn.created)
  )

  val cn = CommentNotification.syntax("cn")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[CommentNotification] = {
    withSQL {
      select.from(CommentNotification as cn).where.eq(cn.id, id)
    }.map(CommentNotification(cn.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[CommentNotification] = {
    withSQL(select.from(CommentNotification as cn)).map(CommentNotification(cn.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(CommentNotification as cn)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[CommentNotification] = {
    withSQL {
      select.from(CommentNotification as cn).where.append(where)
    }.map(CommentNotification(cn.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[CommentNotification] = {
    withSQL {
      select.from(CommentNotification as cn).where.append(where)
    }.map(CommentNotification(cn.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(CommentNotification as cn).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    commentId: Long)(implicit session: DBSession = autoSession): CommentNotification = {
    val generatedKey = withSQL {
      insert.into(CommentNotification).columns(
        column.commentId
      ).values(
        commentId
      )
    }.updateAndReturnGeneratedKey.apply()

    CommentNotification(
      id = generatedKey,
      commentId = commentId)
  }

  def batchInsert(entities: Seq[CommentNotification])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'commentId -> entity.commentId))
        SQL("""insert into COMMENT_NOTIFICATIONS(
        COMMENT_ID
      ) values (
        {commentId}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: CommentNotification)(implicit session: DBSession = autoSession): CommentNotification = {
    withSQL {
      update(CommentNotification).set(
        column.id -> entity.id,
        column.commentId -> entity.commentId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: CommentNotification)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(CommentNotification).where.eq(column.id, entity.id) }.update.apply()
  }

}
