package turqey.entity

import scalikejdbc._
import turqey.servlet.SessionHolder
import org.joda.time.{DateTime}

case class User(
  id: Long,
  loginId: String,
  email: String,
  name: String,
  imgUrl: String,
  password: String,
  root: Boolean = false,
  lastLogin: Option[DateTime] = None,
  created: DateTime = null) {

  def save()(implicit session: DBSession = User.autoSession): User = User.save(this)(session)

  def destroy()(implicit session: DBSession = User.autoSession): Unit = User.destroy(this)(session)

  def editable = { this.self || SessionHolder.root }
  
  def self = { this.id == SessionHolder.user.get.id }
}


object User extends SQLSyntaxSupport[User] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "USERS"

  override val columns = Seq("ID", "LOGIN_ID", "EMAIL", "NAME", "IMG_URL", "PASSWORD", "ROOT", "LAST_LOGIN", "CREATED")

  def apply(u: SyntaxProvider[User])(rs: WrappedResultSet): User = apply(u.resultName)(rs)
  def apply(u: ResultName[User])(rs: WrappedResultSet): User = new User(
    id = rs.get(u.id),
    loginId = rs.get(u.loginId),
    email = rs.get(u.email),
    name = rs.get(u.name),
    imgUrl = rs.get(u.imgUrl),
    password = rs.get(u.password),
    root = rs.get(u.root),
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
    email: String,
    loginId: String,
    name: String,
    imgUrl: String,
    password: String,
    root: Boolean = false,
    lastLogin: Option[DateTime] = None)(implicit session: DBSession = autoSession): User = {
    val generatedKey = withSQL {
      insert.into(User).columns(
        column.loginId,
        column.email,
        column.name,
        column.imgUrl,
        column.password,
        column.root,
        column.lastLogin
      ).values(
        loginId,
        email,
        name,
        imgUrl,
        password,
        root,
        lastLogin
      )
    }.updateAndReturnGeneratedKey.apply()

    User(
      id = generatedKey,
      loginId = loginId,
      email = email,
      name = name,
      imgUrl = imgUrl,
      password = password,
      root = root,
      lastLogin = lastLogin)
  }

  def batchInsert(entities: Seq[User])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'loginId -> entity.loginId,
        'email -> entity.email,
        'name -> entity.name,
        'imgUrl -> entity.imgUrl,
        'password -> entity.password,
        'root -> entity.root,
        'lastLogin -> entity.lastLogin))
        SQL("""insert into USERS(
        LOGIN_ID,
        EMAIL,
        NAME,
        IMG_URL,
        PASSWORD,
        ROOT,
        LAST_LOGIN
      ) values (
        {loginId},
        {email},
        {name},
        {imgUrl},
        {password},
        {root},
        {lastLogin}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: User)(implicit session: DBSession = autoSession): User = {
    withSQL {
      update(User).set(
        column.id -> entity.id,
        column.loginId -> entity.loginId,
        column.email -> entity.email,
        column.name -> entity.name,
        column.imgUrl -> entity.imgUrl,
        column.password -> entity.password,
        column.root -> entity.root,
        column.lastLogin -> entity.lastLogin
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: User)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(User).where.eq(column.id, entity.id) }.update.apply()
  }

}
