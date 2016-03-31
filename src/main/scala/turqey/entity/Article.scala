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
  owner: Option[User] = None,
  published: Boolean = false) {

  def save()(implicit session: DBSession = Article.autoSession): Article = Article.save(this)(session)

  def destroy()(implicit session: DBSession = Article.autoSession): Unit = Article.destroy(this)(session)

  def editable(): Boolean = { turqey.servlet.SessionHolder.user match {
      case Some(u) => u.id == ownerId || u.root
      case None => false
    }
  }
  
  def view(): String = { "" + this.id.toString }

}


object Article extends SQLSyntaxSupport[Article] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "ARTICLES"

  override val columns = Seq("ID", "PROJECT_ID", "TITLE", "CONTENT", "OWNER_ID", "CREATED", "PUBLISHED")

  def apply(a: SyntaxProvider[Article])(rs: WrappedResultSet): Article = apply(a.resultName, None)(rs)
  def apply(a: ResultName[Article])(rs: WrappedResultSet): Article = apply(a, None)(rs)
  def apply(a: ResultName[Article], u: Option[ResultName[User]])(rs: WrappedResultSet): Article = new Article(
    id = rs.get(a.id),
    projectId = rs.get(a.projectId),
    title = rs.get(a.title),
    content = rs.get(a.content),
    ownerId = rs.get(a.ownerId),
    created = rs.get(a.created),
    owner = u.map{ u => User(u)(rs) },
    published = rs.get(a.published)
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
      select.from(Article as a).join(User as u).on(a.ownerId, u.id).orderBy(a.id).desc
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
    val h = ArticleHistory.ah
    withSQL {
      select.from(Article as a)
      .join(User as u).on(a.ownerId, u.id)
      .where.append(where).orderBy(a.id).desc
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
    ownerId: Long,
    published: Boolean = false)(implicit session: DBSession = autoSession): Article = {
    val generatedKey = withSQL {
      insert.into(Article).columns(
        column.projectId,
        column.title,
        column.content,
        column.ownerId,
        column.published
      ).values(
        projectId,
        title,
        content,
        ownerId,
        published
      )
    }.updateAndReturnGeneratedKey.apply()

    Article(
      id = generatedKey,
      projectId = projectId,
      title = title,
      content = content,
      ownerId = ownerId,
      published = published)
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
          OWNER_ID,
          PUBLISHED
        ) values (
          {projectId},
          {title},
          {content},
          {ownerId},
          {published}
        )""").batchByName(params: _*).apply()
    }

  def save(entity: Article)(implicit session: DBSession = autoSession): Article = {
    withSQL {
      update(Article).set(
        column.id -> entity.id,
        column.projectId -> entity.projectId,
        column.title -> entity.title,
        column.content -> entity.content,
        column.ownerId -> entity.ownerId,
        column.published -> entity.published
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
  
  case class ArticleForList(
    id: Long,
    title: String,
    created: String,
    updated: String,
    owner: UserForList,
    tags: Seq[Tag],
    stock: Long,
    comment: Long)
  case class UserForList(id: Long, name: String)
  object UserForList{ def apply(user: User) = new UserForList(user.id, user.name) }
  
  def findForList(ids: Seq[Long])(implicit session: DBSession = autoSession): Seq[ArticleForList] = {
    val tagsOfArticleIds = Tag.findTagsOfArticleIds(ids)
    val lastUpdatesByIds = ArticleHistory.findLatestsByIds(ids)
    val articles = Article.findAllBy(sqls.in(Article.a.id, ids))
    val stockCountByArticleIds = ArticleStock.countByIds(ids)
    val commentCountByArticleIds = ArticleComment.countByIds(ids)
    
    articles.map{ a => ArticleForList(
      id      = a.id,
      title   = a.title,
      created = a.created.toString("yyyy/MM/dd"),
      updated = lastUpdatesByIds.get(a.id).map(_.toString("yyyy/MM/dd")).getOrElse(""),
      owner   = UserForList(a.owner.get),
      tags    = tagsOfArticleIds.getOrElse(a.id, Seq()).map(_._2),
      stock   = stockCountByArticleIds.getOrElse(a.id, 0),
      comment = commentCountByArticleIds.getOrElse(a.id, 0)
    ) }
  }
  
  def findAllId()(implicit session: DBSession = autoSession) :Seq[Long] = {
    withSQL {
      select(a.result.id).from(Article as a).orderBy(a.id).desc
    }.map(_.long(1)).list.apply()
  }
  
  def findAllIdBy(where: SQLSyntax)(implicit session: DBSession = autoSession) :Seq[Long] = {
    withSQL {
      select(a.result.id).from(Article as a)
      .where.append(where).orderBy(a.id).desc
    }.map(_.long(1)).list.apply()
  }

  def getStockers(id: Long)(implicit session: DBSession = autoSession) :Seq[User] = {
    val u = User.u
    val as = ArticleStock.as
    withSQL {
      select.from(User as u)
      .where.exists( 
        select.from(ArticleStock as as)
        .where.eq(as.articleId, id).and.eq(as.userId, u.id)
      ).orderBy(u.resultName.id).desc
    }.map(User(u.resultName)(_)).list.apply()
  }

}
