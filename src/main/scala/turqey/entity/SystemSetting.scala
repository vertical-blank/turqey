package turqey.entity

import scalikejdbc._

case class SystemSetting(
  key: String,
  value: String) {

  def save()(implicit session: DBSession = SystemSetting.autoSession): SystemSetting = SystemSetting.save(this)(session)

  def destroy()(implicit session: DBSession = SystemSetting.autoSession): Unit = SystemSetting.destroy(this)(session)

}

object Keys {
  val baseUrl     = "baseUrl"
}

case class SmtpSettings(
  host: String,
  port: Int,
  user: String,
  password: String,
  ssl: Boolean,
  fromAddr: String,
  fromName: String
) {
  def toSeq: Seq[SystemSetting] = {
    Seq(
      SystemSetting(SmtpSettings.Keys.host, host),
      SystemSetting(SmtpSettings.Keys.port, port.toString),
      SystemSetting(SmtpSettings.Keys.user, user),
      SystemSetting(SmtpSettings.Keys.password, password),
      SystemSetting(SmtpSettings.Keys.ssl, ssl.toString),
      SystemSetting(SmtpSettings.Keys.fromAddr, fromAddr),
      SystemSetting(SmtpSettings.Keys.fromName, fromName)
    )
  }
}

object SmtpSettings {
  object Keys {
    val enabled   = "smtpEnabled"
    val host      = "smtpHost"
    val port      = "smtpPort"
    val user      = "smtpUser"
    val password  = "smtpPassword"
    val ssl       = "smtpSSL"
    val fromAddr  = "smtpFromAddr"
    val fromName  = "smtpFromName"
    
    def all() = Seq(host, port, user, password, ssl, fromAddr, fromName)
  }
  
  def apply(vals: Seq[SystemSetting]):Option[SmtpSettings] = {
    apply(vals.map(s => (s.key, s.value)).toMap)
  }
  def apply(vals: Map[String, String]):Option[SmtpSettings] = {
    for {
      host <- vals.get(Keys.host)
      port <- vals.get(Keys.port)
      user <- vals.get(Keys.user)
      password <- vals.get(Keys.password)
      ssl <- vals.get(Keys.ssl)
      fromAddr <- vals.get(Keys.fromAddr)
      fromName <- vals.get(Keys.fromName)
    } yield new SmtpSettings(
      host,
      port.toInt,
      user,
      password,
      ssl.toBoolean,
      fromAddr,
      fromName
    )
  }
}

object SystemSetting extends SQLSyntaxSupport[SystemSetting] {

  override val schemaName = Some("PUBLIC")

  override val tableName = "SYSTEM_SETTINGS"

  override val columns = Seq("KEY", "VALUE")

  def apply(ss: SyntaxProvider[SystemSetting])(rs: WrappedResultSet): SystemSetting = apply(ss.resultName)(rs)
  def apply(ss: ResultName[SystemSetting])(rs: WrappedResultSet): SystemSetting = new SystemSetting(
    key = rs.get(ss.key),
    value = rs.get(ss.value)
  )

  val ss = SystemSetting.syntax("ss")

  override val autoSession = AutoSession

  def find(key: String)(implicit session: DBSession = autoSession): Option[SystemSetting] = {
    withSQL {
      select.from(SystemSetting as ss).where.eq(ss.key, key)
    }.map(SystemSetting(ss.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[SystemSetting] = {
    withSQL(select.from(SystemSetting as ss)).map(SystemSetting(ss.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(SystemSetting as ss)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[SystemSetting] = {
    withSQL {
      select.from(SystemSetting as ss).where.append(where)
    }.map(SystemSetting(ss.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[SystemSetting] = {
    withSQL {
      select.from(SystemSetting as ss).where.append(where)
    }.map(SystemSetting(ss.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(SystemSetting as ss).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    key: String,
    value: String)(implicit session: DBSession = autoSession): SystemSetting = {
    withSQL {
      insert.into(SystemSetting).columns(
        column.key,
        column.value
      ).values(
        key,
        value
      )
    }.update.apply()

    SystemSetting(
      key = key,
      value = value)
  }

  def batchInsert(entities: Seq[SystemSetting])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity => 
      Seq(
        'key -> entity.key,
        'value -> entity.value))
        SQL("""insert into SYSTEM_SETTINGS(
        KEY,
        VALUE
      ) values (
        {key},
        {value}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: SystemSetting)(implicit session: DBSession = autoSession): SystemSetting = {
    withSQL {
      update(SystemSetting).set(
        column.key -> entity.key,
        column.value -> entity.value
      ).where.eq(column.key, entity.key)
    }.update.apply()
    entity
  }

  def destroy(entity: SystemSetting)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(SystemSetting).where.eq(column.key, entity.key) }.update.apply()
  }

}
