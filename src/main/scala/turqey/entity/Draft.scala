package turqey.entity

import scalikejdbc._
import org.joda.time.{DateTime}
import java.sql.{Clob}

case class Draft(
  id: Long,
  articleId: Option[Long] = None,
  title: String,
  content: Clob,
  ownerId: Long,
  created: DateTime = null) {

  def save()(implicit session: DBSession = Draft.autoSession): Draft = Draft.save(this)(session)

  def destroy()(implicit session: DBSession = Draft.autoSession): Unit = Draft.destroy(this)(session)

}


object Draft extends SQLSyntaxSupport[Draft] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "DRAFTS"

  override val columns = Seq("ID", "ARTICLE_ID", "TITLE", "CONTENT", "OWNER_ID", "CREATED")

  def apply(d: SyntaxProvider[Draft])(rs: WrappedResultSet): Draft = apply(d.resultName)(rs)
  def apply(d: ResultName[Draft])(rs: WrappedResultSet): Draft = new Draft(
    id = rs.get(d.id),
    articleId = rs.get(d.articleId),
    title = rs.get(d.title),
    content = rs.get(d.content),
    ownerId = rs.get(d.ownerId),
    created = rs.get(d.created)
  )

  val d = Draft.syntax("d")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Draft] = {
    withSQL {
      select.from(Draft as d).where.eq(d.id, id)
    }.map(Draft(d.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Draft] = {
    withSQL(select.from(Draft as d)).map(Draft(d.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Draft as d)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Draft] = {
    withSQL {
      select.from(Draft as d).where.append(where)
    }.map(Draft(d.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Draft] = {
    withSQL {
      select.from(Draft as d).where.append(where)
    }.map(Draft(d.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Draft as d).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    articleId: Option[Long] = None,
    title: String,
    content: Clob,
    ownerId: Long)(implicit session: DBSession = autoSession): Draft = {
    val generatedKey = withSQL {
      insert.into(Draft).columns(
        column.articleId,
        column.title,
        column.content,
        column.ownerId
      ).values(
        articleId,
        title,
        content,
        ownerId
      )
    }.updateAndReturnGeneratedKey.apply()

    Draft(
      id = generatedKey,
      articleId = articleId,
      title = title,
      content = content,
      ownerId = ownerId)
  }

  def batchInsert(entities: Seq[Draft])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'articleId -> entity.articleId,
        'title -> entity.title,
        'content -> entity.content,
        'ownerId -> entity.ownerId,
        'created -> entity.created))
        SQL("""insert into DRAFTS(
        ARTICLE_ID,
        TITLE,
        CONTENT,
        OWNER_ID,
        CREATED
      ) values (
        {articleId},
        {title},
        {content},
        {ownerId},
        {created}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: Draft)(implicit session: DBSession = autoSession): Draft = {
    withSQL {
      update(Draft).set(
        column.id -> entity.id,
        column.articleId -> entity.articleId,
        column.title -> entity.title,
        column.content -> entity.content,
        column.ownerId -> entity.ownerId,
        column.created -> entity.created
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Draft)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Draft).where.eq(column.id, entity.id) }.update.apply()
  }

}
