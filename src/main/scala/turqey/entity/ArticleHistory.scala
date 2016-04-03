package turqey.entity

import scalikejdbc._
import org.joda.time.{DateTime}

case class ArticleHistory(
  id: Long,
  articleId: Long,
  commitId: String,
  userId: Option[Long] = None,
  created: DateTime = null) {

  def save()(implicit session: DBSession = ArticleHistory.autoSession): ArticleHistory = ArticleHistory.save(this)(session)

  def destroy()(implicit session: DBSession = ArticleHistory.autoSession): Unit = ArticleHistory.destroy(this)(session)

}


object ArticleHistory extends SQLSyntaxSupport[ArticleHistory] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "ARTICLE_HISTORIES"

  override val columns = Seq("ID", "ARTICLE_ID", "COMMIT_ID", "USER_ID", "CREATED")

  def apply(ah: SyntaxProvider[ArticleHistory])(rs: WrappedResultSet): ArticleHistory = apply(ah.resultName)(rs)
  def apply(ah: ResultName[ArticleHistory])(rs: WrappedResultSet): ArticleHistory = new ArticleHistory(
    id = rs.get(ah.id),
    articleId = rs.get(ah.articleId),
    commitId = rs.get(ah.commitId),
    userId = rs.get(ah.userId),
    created = rs.get(ah.created)
  )

  val ah = ArticleHistory.syntax("ah")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[ArticleHistory] = {
    withSQL {
      select.from(ArticleHistory as ah).where.eq(ah.id, id)
    }.map(ArticleHistory(ah.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[ArticleHistory] = {
    withSQL(select.from(ArticleHistory as ah)).map(ArticleHistory(ah.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(ArticleHistory as ah)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[ArticleHistory] = {
    withSQL {
      select.from(ArticleHistory as ah).where.append(where)
    }.map(ArticleHistory(ah.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ArticleHistory] = {
    withSQL {
      select.from(ArticleHistory as ah).where.append(where)
    }.map(ArticleHistory(ah.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(ArticleHistory as ah).where.append(where)
    }.map(_.long(1)).single.apply().get
  }
  def create(
    articleId: Long,
    commitId: String,
    userId: Option[Long] = None,
    created: DateTime = null
    )(implicit session: DBSession = autoSession): ArticleHistory = {
    val generatedKey = withSQL {
      if (created != null){
        insert.into(ArticleHistory).columns(
          column.articleId,
          column.commitId,
          column.userId,
          column.created
        ).values(
          articleId,
          commitId,
          userId,
          created
        )
      }
      else {
        insert.into(ArticleHistory).columns(
          column.articleId,
          column.commitId,
          column.userId
        ).values(
          articleId,
          commitId,
          userId
        )
      }
    }.updateAndReturnGeneratedKey.apply()

    ArticleHistory(
      id = generatedKey,
      articleId = articleId,
      commitId = commitId,
      userId = userId)
  }

  def batchInsert(entities: Seq[ArticleHistory])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'articleId -> entity.articleId,
        'commitId -> entity.commitId,
        'userId -> entity.userId))
        SQL("""insert into ARTICLE_HISTORIES(
        ARTICLE_ID,
        COMMIT_ID,
        USER_ID
      ) values (
        {articleId},
        {commitId},
        {userId}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: ArticleHistory)(implicit session: DBSession = autoSession): ArticleHistory = {
    withSQL {
      update(ArticleHistory).set(
        column.id -> entity.id,
        column.articleId -> entity.articleId,
        column.commitId -> entity.commitId,
        column.userId -> entity.userId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: ArticleHistory)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(ArticleHistory).where.eq(column.id, entity.id) }.update.apply()
  }
  
  def findLatestsByIds(articleIds: Seq[Long])(implicit session: DBSession = autoSession): Map[Long, DateTime] = {
    withSQL {
      select(ah.articleId, ah.created).from(ArticleHistory as ah)
      .where.in(ah.articleId, articleIds)
    }.map{ rs => ( rs.long(1), rs.jodaDateTime(2) ) }.list.apply().toMap
  }

}
