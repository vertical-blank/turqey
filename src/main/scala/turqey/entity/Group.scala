package turqey.entity

import scalikejdbc._

case class Group(
  id: Long,
  name: String,
  owner: Long) {

  def save()(implicit session: DBSession = Group.autoSession): Group = Group.save(this)(session)

  def destroy()(implicit session: DBSession = Group.autoSession): Unit = Group.destroy(this)(session)

}


object Group extends SQLSyntaxSupport[Group] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "GROUPS"

  override val columns = Seq("ID", "NAME", "OWNER")

  def apply(g: SyntaxProvider[Group])(rs: WrappedResultSet): Group = apply(g.resultName)(rs)
  def apply(g: ResultName[Group])(rs: WrappedResultSet): Group = new Group(
    id = rs.get(g.id),
    name = rs.get(g.name),
    owner = rs.get(g.owner)
  )

  val g = Group.syntax("g")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Group] = {
    withSQL {
      select.from(Group as g).where.eq(g.id, id)
    }.map(Group(g.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Group] = {
    withSQL(select.from(Group as g)).map(Group(g.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Group as g)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Group] = {
    withSQL {
      select.from(Group as g).where.append(where)
    }.map(Group(g.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Group] = {
    withSQL {
      select.from(Group as g).where.append(where)
    }.map(Group(g.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Group as g).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    name: String,
    owner: Long)(implicit session: DBSession = autoSession): Group = {
    val generatedKey = withSQL {
      insert.into(Group).columns(
        column.name,
        column.owner
      ).values(
        name,
        owner
      )
    }.updateAndReturnGeneratedKey.apply()

    Group(
      id = generatedKey,
      name = name,
      owner = owner)
  }

  def batchInsert(entities: Seq[Group])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'name -> entity.name,
        'owner -> entity.owner))
        SQL("""insert into GROUPS(
        NAME,
        OWNER
      ) values (
        {name},
        {owner}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: Group)(implicit session: DBSession = autoSession): Group = {
    withSQL {
      update(Group).set(
        column.id -> entity.id,
        column.name -> entity.name,
        column.owner -> entity.owner
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Group)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Group).where.eq(column.id, entity.id) }.update.apply()
  }

}
