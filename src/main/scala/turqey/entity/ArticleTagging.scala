package turqey.entity

import scalikejdbc._
import org.joda.time.{DateTime}

case class ArticleTagging(
  id: Long,
  articleId: Long,
  tagId: Long,
  created: DateTime = null) {

  def save()(implicit session: DBSession = ArticleTagging.autoSession): ArticleTagging = ArticleTagging.save(this)(session)

  def destroy()(implicit session: DBSession = ArticleTagging.autoSession): Unit = ArticleTagging.destroy(this)(session)

}


object ArticleTagging extends SQLSyntaxSupport[ArticleTagging] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "ARTICLE_TAGGINGS"

  override val columns = Seq("ID", "ARTICLE_ID", "TAG_ID", "CREATED")

  def apply(at: SyntaxProvider[ArticleTagging])(rs: WrappedResultSet): ArticleTagging = apply(at.resultName)(rs)
  def apply(at: ResultName[ArticleTagging])(rs: WrappedResultSet): ArticleTagging = new ArticleTagging(
    id = rs.get(at.id),
    articleId = rs.get(at.articleId),
    tagId = rs.get(at.tagId),
    created = rs.get(at.created)
  )

  val at = ArticleTagging.syntax("at")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[ArticleTagging] = {
    withSQL {
      select.from(ArticleTagging as at).where.eq(at.id, id)
    }.map(ArticleTagging(at.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[ArticleTagging] = {
    withSQL(select.from(ArticleTagging as at)).map(ArticleTagging(at.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(ArticleTagging as at)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[ArticleTagging] = {
    withSQL {
      select.from(ArticleTagging as at).where.append(where)
    }.map(ArticleTagging(at.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ArticleTagging] = {
    withSQL {
      select.from(ArticleTagging as at).where.append(where)
    }.map(ArticleTagging(at.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(ArticleTagging as at).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    articleId: Long,
    tagId: Long)(implicit session: DBSession = autoSession): ArticleTagging = {
    val generatedKey = withSQL {
      insert.into(ArticleTagging).columns(
        column.articleId,
        column.tagId
      ).values(
        articleId,
        tagId
      )
    }.updateAndReturnGeneratedKey.apply()

    ArticleTagging(
      id = generatedKey,
      articleId = articleId,
      tagId = tagId)
  }

  def batchInsert(entities: Seq[ArticleTagging])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'articleId -> entity.articleId,
        'tagId -> entity.tagId))
        SQL("""insert into ARTICLE_TAGGINGS(
        ARTICLE_ID,
        TAG_ID
      ) values (
        {articleId},
        {tagId}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: ArticleTagging)(implicit session: DBSession = autoSession): ArticleTagging = {
    withSQL {
      update(ArticleTagging).set(
        column.id -> entity.id,
        column.articleId -> entity.articleId,
        column.tagId -> entity.tagId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: ArticleTagging)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(ArticleTagging).where.eq(column.id, entity.id) }.update.apply()
  }

  def deleteTagsOfArticle(articleId: Long, tagIds: Seq[Long])(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(ArticleTagging).where.eq(column.articleId, articleId).and.in(column.tagId, tagIds) }.update.apply()
  }

  def insertTagsOfArticle(articleId: Long, tagIds: Seq[Long])(implicit session: DBSession = autoSession): Unit = {
    Array.fill(tagIds.size)(articleId).zip(tagIds).foreach( x => this.create(x._1, x._2) )
  }

  def countByTag()(implicit session: DBSession = autoSession): Seq[(Long, Int)] = {
    val at = ArticleTagging.at

    withSQL {
      select(at.tagId, sqls.count)
        .from(ArticleTagging as at)
        .groupBy(at.tagId)
        .orderBy(sqls.count)
    }.map( rs => (rs.long(1), rs.int(2)) ).list.apply()
  }

}
