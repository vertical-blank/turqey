package turqey.entity

import scalikejdbc._
import org.joda.time.{DateTime}
import java.sql.{Clob}

case class ArticleComment(
  id: Long,
  articleId: Long,
  userId: Long,
  content: Clob,
  created: DateTime = null,
  user: Option[User] = None) {

  def save()(implicit session: DBSession = ArticleComment.autoSession): ArticleComment = ArticleComment.save(this)(session)

  def destroy()(implicit session: DBSession = ArticleComment.autoSession): Unit = ArticleComment.destroy(this)(session)

  def editable(): Boolean = { turqey.servlet.SessionHolder.user match {
      case Some(u) => u.id == userId
      case None => false
    }
  }

}


object ArticleComment extends SQLSyntaxSupport[ArticleComment] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "ARTICLE_COMMENTS"

  override val columns = Seq("ID", "ARTICLE_ID", "USER_ID", "CONTENT", "CREATED")

  def apply(ac: SyntaxProvider[ArticleComment])(rs: WrappedResultSet): ArticleComment = apply(ac.resultName, None)(rs)
  def apply(ac: ResultName[ArticleComment], u: Option[ResultName[User]])(rs: WrappedResultSet): ArticleComment = new ArticleComment(
    id = rs.get(ac.id),
    articleId = rs.get(ac.articleId),
    userId = rs.get(ac.userId),
    content = rs.get(ac.content),
    created = rs.get(ac.created),
    user = u.map{ u => User(u)(rs) }
  )

  val ac = ArticleComment.syntax("ac")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[ArticleComment] = {
    withSQL {
      select.from(ArticleComment as ac).where.eq(ac.id, id)
    }.map(ArticleComment(ac.resultName, None)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[ArticleComment] = {
    withSQL(select.from(ArticleComment as ac)).map(ArticleComment(ac.resultName, None)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(ArticleComment as ac)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[ArticleComment] = {
    withSQL {
      select.from(ArticleComment as ac).where.append(where)
    }.map(ArticleComment(ac.resultName, None)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ArticleComment] = {
    val u = User.u
    withSQL {
      select.from(ArticleComment as ac).join(User as u).on(ac.userId, u.id).where.append(where)
    }.map(ArticleComment(ac.resultName, Option(u.resultName))).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(ArticleComment as ac).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    articleId: Long,
    userId: Long,
    content: Clob)(implicit session: DBSession = autoSession): ArticleComment = {
    val generatedKey = withSQL {
      insert.into(ArticleComment).columns(
        column.articleId,
        column.userId,
        column.content
      ).values(
        articleId,
        userId,
        content
      )
    }.updateAndReturnGeneratedKey.apply()

    ArticleComment(
      id = generatedKey,
      articleId = articleId,
      userId = userId,
      content = content)
  }

  def batchInsert(entities: Seq[ArticleComment])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'articleId -> entity.articleId,
        'userId -> entity.userId,
        'content -> entity.content))
        SQL("""insert into ARTICLE_COMMENTS(
        ARTICLE_ID,
        USER_ID,
        CONTENT
      ) values (
        {articleId},
        {userId},
        {content}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: ArticleComment)(implicit session: DBSession = autoSession): ArticleComment = {
    withSQL {
      update(ArticleComment).set(
        column.id -> entity.id,
        column.articleId -> entity.articleId,
        column.userId -> entity.userId,
        column.content -> entity.content
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: ArticleComment)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(ArticleComment).where.eq(column.id, entity.id) }.update.apply()
  }

}
