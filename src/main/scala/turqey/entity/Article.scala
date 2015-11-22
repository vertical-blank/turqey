package turqey.entity

import scalikejdbc._
import org.joda.time.{DateTime}
import java.sql.{Clob}

case class Article(
  id: Long,
  projectId: Option[Long] = None,
  title: String,
  content: Clob,
  owner: Long,
  created: DateTime = null) {

  def save()(implicit session: DBSession = Article.autoSession): Article = Article.save(this)(session)

  def destroy()(implicit session: DBSession = Article.autoSession): Unit = Article.destroy(this)(session)

}


object Article extends SQLSyntaxSupport[Article] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "ARTICLES"

  override val columns = Seq("ID", "PROJECT_ID", "TITLE", "CONTENT", "OWNER", "CREATED")

  def apply(a: SyntaxProvider[Article])(rs: WrappedResultSet): Article = apply(a.resultName)(rs)
  def apply(a: ResultName[Article])(rs: WrappedResultSet): Article = new Article(
    id = rs.get(a.id),
    projectId = rs.get(a.projectId),
    title = rs.get(a.title),
    content = rs.get(a.content),
    owner = rs.get(a.owner),
    created = rs.get(a.created)
  )

  val a = Article.syntax("a")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Article] = {
    withSQL {
      select.from(Article as a).where.eq(a.id, id)
    }.map(Article(a.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Article] = {
    withSQL(select.from(Article as a)).map(Article(a.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Article as a)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Article] = {
    withSQL {
      select.from(Article as a).where.append(where)
    }.map(Article(a.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Article] = {
    withSQL {
      select.from(Article as a).where.append(where)
    }.map(Article(a.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Article as a).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    projectId: Option[Long] = None,
    title: String,
    content: Clob,
    owner: Long)(implicit session: DBSession = autoSession): Article = {
    val generatedKey = withSQL {
      insert.into(Article).columns(
        column.projectId,
        column.title,
        column.content,
        column.owner
      ).values(
        projectId,
        title,
        content,
        owner
      )
    }.updateAndReturnGeneratedKey.apply()

    Article(
      id = generatedKey,
      projectId = projectId,
      title = title,
      content = content,
      owner = owner)
  }

  def batchInsert(entities: Seq[Article])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'projectId -> entity.projectId,
        'title -> entity.title,
        'content -> entity.content,
        'owner -> entity.owner))
        SQL("""insert into ARTICLES(
        PROJECT_ID,
        TITLE,
        CONTENT,
        OWNER
      ) values (
        {projectId},
        {title},
        {content},
        {owner}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: Article)(implicit session: DBSession = autoSession): Article = {
    withSQL {
      update(Article).set(
        column.id -> entity.id,
        column.projectId -> entity.projectId,
        column.title -> entity.title,
        column.content -> entity.content,
        column.owner -> entity.owner
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Article)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Article).where.eq(column.id, entity.id) }.update.apply()
  }

  def findTagged(tagId: Long)(implicit session: DBSession = autoSession): Seq[Article] = {
    withSQL {
      val a = Article.a
      val at = ArticleTagging.at
      select.from(Article as a).where.exists(
        select.from(ArticleTagging as at).where.eq(a.id, at.articleId).and.eq(at.tagId, tagId)
      )
    }.map(Article(a.resultName)).list.apply()
  }

}
