package turqey.entity

import scalikejdbc._
import org.joda.time.{DateTime}

case class User(
  id: Long,
  email: Option[String] = None,
  lastLogin: Option[DateTime] = None,
  created: Option[DateTime] = None) {

  def save()(implicit session: DBSession = User.autoSession): User = User.save(this)(session)

  def destroy()(implicit session: DBSession = User.autoSession): Unit = User.destroy(this)(session)

}


object User extends SQLSyntaxSupport[User] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "USERS"

  override val columns = Seq("ID", "EMAIL", "LAST_LOGIN", "CREATED")

  def apply(u: SyntaxProvider[User])(rs: WrappedResultSet): User = apply(u.resultName)(rs)
  def apply(u: ResultName[User])(rs: WrappedResultSet): User = new User(
    id = rs.get(u.id),
    email = rs.get(u.email),
    lastLogin = rs.get(u.lastLogin),
    created = rs.get(u.created)
  )

  val u = User.syntax("u")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[User] = {
    withSQL {
      select.from(User as u).where.eq(u.id, id)
    }.map(User(u.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[User] = {
    withSQL(select.from(User as u)).map(User(u.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(User as u)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[User] = {
    withSQL {
      select.from(User as u).where.append(where)
    }.map(User(u.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[User] = {
    withSQL {
      select.from(User as u).where.append(where)
    }.map(User(u.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(User as u).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    email: Option[String] = None,
    lastLogin: Option[DateTime] = None,
    created: Option[DateTime] = None)(implicit session: DBSession = autoSession): User = {
    val generatedKey = withSQL {
      insert.into(User).columns(
        column.email,
        column.lastLogin,
        column.created
      ).values(
        email,
        lastLogin,
        created
      )
    }.updateAndReturnGeneratedKey.apply()

    User(
      id = generatedKey,
      email = email,
      lastLogin = lastLogin,
      created = created)
  }

  def batchInsert(entities: Seq[User])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'email -> entity.email,
        'lastLogin -> entity.lastLogin,
        'created -> entity.created))
        SQL("""insert into USERS(
        EMAIL,
        LAST_LOGIN,
        CREATED
      ) values (
        {email},
        {lastLogin},
        {created}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: User)(implicit session: DBSession = autoSession): User = {
    withSQL {
      update(User).set(
        column.id -> entity.id,
        column.email -> entity.email,
        column.lastLogin -> entity.lastLogin,
        column.created -> entity.created
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: User)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(User).where.eq(column.id, entity.id) }.update.apply()
  }

}
