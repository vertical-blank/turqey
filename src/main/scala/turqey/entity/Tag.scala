package turqey.entity

import scalikejdbc._

case class Tag(
  id: Long,
  name: String,
  icon: Option[String] = None) {

  def save()(implicit session: DBSession = Tag.autoSession): Tag = Tag.save(this)(session)

  def destroy()(implicit session: DBSession = Tag.autoSession): Unit = Tag.destroy(this)(session)

}

case class TagAndCount(tag:Tag, count: Int)

object Tag extends SQLSyntaxSupport[Tag] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "TAGS"

  override val columns = Seq("ID", "NAME", "ICON")

  def apply(t: SyntaxProvider[Tag])(rs: WrappedResultSet): Tag = apply(t.resultName)(rs)
  def apply(t: ResultName[Tag])(rs: WrappedResultSet): Tag = new Tag(
    id = rs.get(t.id),
    name = rs.get(t.name),
    icon = rs.get(t.icon)
  )

  val t = Tag.syntax("t")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Tag] = {
    withSQL {
      select.from(Tag as t).where.eq(t.id, id)
    }.map(Tag(t.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Tag] = {
    withSQL(select.from(Tag as t)).map(Tag(t.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Tag as t)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Tag] = {
    withSQL {
      select.from(Tag as t).where.append(where)
    }.map(Tag(t.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Tag] = {
    withSQL {
      select.from(Tag as t).where.append(where)
    }.map(Tag(t.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Tag as t).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    name: String,
    icon: Option[String] = None)(implicit session: DBSession = autoSession): Tag = {
    val generatedKey = withSQL {
      insert.into(Tag).columns(
        column.name,
        column.icon
      ).values(
        name,
        icon
      )
    }.updateAndReturnGeneratedKey.apply()

    Tag(
      id = generatedKey,
      name = name,
      icon = icon)
  }

  def batchInsert(entities: Seq[Tag])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'name -> entity.name,
        'icon -> entity.icon))
        SQL("""insert into TAGS(
        NAME,
        ICON
      ) values (
        {name},
        {icon}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: Tag)(implicit session: DBSession = autoSession): Tag = {
    withSQL {
      update(Tag).set(
        column.id -> entity.id,
        column.name -> entity.name,
        column.icon -> entity.icon
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Tag)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Tag).where.eq(column.id, entity.id) }.update.apply()
  }

  def findAllWithArticleCount(): Seq[(Tag, Int)] = {
    val allTags = this.findAll()
    val countByTag = ArticleTagging.countByTag().toMap

    allTags map { x => ( x, countByTag.getOrElse(x.id, 0) ) }
  }

}
