package turqey.entity

import scalikejdbc._
import org.joda.time.{DateTime}
import java.sql.{Clob}

case class Article(
  id: Long,
  projectId: Option[Long] = None,
  title: String,
  content: Clob,
  ownerId: Long,
  created: DateTime = null,
  owner: Option[User] = None) {

  def save()(implicit session: DBSession = Article.autoSession): Article = Article.save(this)(session)

  def destroy()(implicit session: DBSession = Article.autoSession): Unit = Article.destroy(this)(session)

  def editable(): Boolean = { turqey.servlet.SessionHolder.user match {
      case Some(u) => u.id == ownerId || u.root
      case None => false
    }
  }

}


object Article extends SQLSyntaxSupport[Article] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "ARTICLES"

  override val columns = Seq("ID", "PROJECT_ID", "TITLE", "CONTENT", "OWNER_ID", "CREATED")

  def apply(a: SyntaxProvider[Article])(rs: WrappedResultSet): Article = apply(a.resultName, None)(rs)
  def apply(a: ResultName[Article])(rs: WrappedResultSet): Article = apply(a, None)(rs)
  def apply(a: ResultName[Article], u: Option[ResultName[User]])(rs: WrappedResultSet): Article = new Article(
    id = rs.get(a.id),
    projectId = rs.get(a.projectId),
    title = rs.get(a.title),
    content = rs.get(a.content),
    ownerId = rs.get(a.ownerId),
    created = rs.get(a.created),
    owner = u.map{ u => User(u)(rs) }
  )

  val a = Article.syntax("a")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Article] = {
    val u = User.u
    withSQL {
      select
      .from(Article as a)
      .join(User as u).on(a.ownerId, u.id)
      .where.eq(a.id, id)
    }.map( Article(a.resultName, Option(u.resultName)) ).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Article] = {
    val u = User.u
    withSQL{
      select.from(Article as a).join(User as u).on(a.ownerId, u.id)
    }.map(Article(a.resultName, Option(u.resultName))).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Article as a)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Article] = {
    val u = User.u
    withSQL {
      select.from(Article as a).join(User as u).on(a.ownerId, u.id).where.append(where)
    }.map(Article(a.resultName, Option(u.resultName))).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Article] = {
    val u = User.u
    withSQL {
      select.from(Article as a).join(User as u).on(a.ownerId, u.id).where.append(where)
    }.map(Article(a.resultName, Option(u.resultName))).list.apply()
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
    ownerId: Long)(implicit session: DBSession = autoSession): Article = {
    val generatedKey = withSQL {
      insert.into(Article).columns(
        column.projectId,
        column.title,
        column.content,
        column.ownerId
      ).values(
        projectId,
        title,
        content,
        ownerId
      )
    }.updateAndReturnGeneratedKey.apply()

    Article(
      id = generatedKey,
      projectId = projectId,
      title = title,
      content = content,
      ownerId = ownerId)
  }

  def batchInsert(entities: Seq[Article])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'projectId -> entity.projectId,
        'title -> entity.title,
        'content -> entity.content,
        'ownerId -> entity.ownerId))
        SQL("""insert into ARTICLES(
        PROJECT_ID,
        TITLE,
        CONTENT,
        OWNER_ID
      ) values (
        {projectId},
        {title},
        {content},
        {ownerId}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: Article)(implicit session: DBSession = autoSession): Article = {
    withSQL {
      update(Article).set(
        column.id -> entity.id,
        column.projectId -> entity.projectId,
        column.title -> entity.title,
        column.content -> entity.content,
        column.ownerId -> entity.ownerId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Article)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Article).where.eq(column.id, entity.id) }.update.apply()
  }

  def findTagged(tagId: Long)(implicit session: DBSession = autoSession): Seq[Article] = {
    val u = User.u
    withSQL {
      val a = Article.a
      val at = ArticleTagging.at
      select.from(Article as a).join(User as u).on(a.ownerId, u.id).where.exists(
        select.from(ArticleTagging as at).where.eq(a.id, at.articleId).and.eq(at.tagId, tagId)
      )
    }.map(Article(a.resultName, Option(u.resultName))).list.apply()
  }

}
