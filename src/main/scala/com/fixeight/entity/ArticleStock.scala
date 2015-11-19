package com.fixeight.entity

import scalikejdbc._

case class ArticleStock(
  id: Long,
  articleId: Long,
  userId: Long) {

  def save()(implicit session: DBSession = ArticleStock.autoSession): ArticleStock = ArticleStock.save(this)(session)

  def destroy()(implicit session: DBSession = ArticleStock.autoSession): Unit = ArticleStock.destroy(this)(session)

}


object ArticleStock extends SQLSyntaxSupport[ArticleStock] {

  override val tableName = "ARTICLE_STOCKS"

  override val columns = Seq("ID", "ARTICLE_ID", "USER_ID")

  def apply(as: SyntaxProvider[ArticleStock])(rs: WrappedResultSet): ArticleStock = apply(as.resultName)(rs)
  def apply(as: ResultName[ArticleStock])(rs: WrappedResultSet): ArticleStock = new ArticleStock(
    id = rs.get(as.id),
    articleId = rs.get(as.articleId),
    userId = rs.get(as.userId)
  )

  val as = ArticleStock.syntax("as")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[ArticleStock] = {
    withSQL {
      select.from(ArticleStock as as).where.eq(as.id, id)
    }.map(ArticleStock(as.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[ArticleStock] = {
    withSQL(select.from(ArticleStock as as)).map(ArticleStock(as.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(ArticleStock as as)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[ArticleStock] = {
    withSQL {
      select.from(ArticleStock as as).where.append(where)
    }.map(ArticleStock(as.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ArticleStock] = {
    withSQL {
      select.from(ArticleStock as as).where.append(where)
    }.map(ArticleStock(as.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(ArticleStock as as).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    articleId: Long,
    userId: Long)(implicit session: DBSession = autoSession): ArticleStock = {
    val generatedKey = withSQL {
      insert.into(ArticleStock).columns(
        column.articleId,
        column.userId
      ).values(
        articleId,
        userId
      )
    }.updateAndReturnGeneratedKey.apply()

    ArticleStock(
      id = generatedKey,
      articleId = articleId,
      userId = userId)
  }

  def batchInsert(entities: Seq[ArticleStock])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'articleId -> entity.articleId,
        'userId -> entity.userId))
        SQL("""insert into ARTICLE_STOCKS(
        ARTICLE_ID,
        USER_ID
      ) values (
        {articleId},
        {userId}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: ArticleStock)(implicit session: DBSession = autoSession): ArticleStock = {
    withSQL {
      update(ArticleStock).set(
        column.id -> entity.id,
        column.articleId -> entity.articleId,
        column.userId -> entity.userId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: ArticleStock)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(ArticleStock).where.eq(column.id, entity.id) }.update.apply()
  }

}
