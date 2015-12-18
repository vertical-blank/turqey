package turqey.entity

import scalikejdbc._

case class SystemSetting(
  key: String,
  value: Option[String] = None) {

  def save()(implicit session: DBSession = SystemSetting.autoSession): SystemSetting = SystemSetting.save(this)(session)

  def destroy()(implicit session: DBSession = SystemSetting.autoSession): Unit = SystemSetting.destroy(this)(session)

}

case class SmtpSettings(
  smtpHost: String,
  smtpPort: Int,
  smtpUser: String,
  smtpPassword: String,
  smtpSSL: Boolean,
  smtpFromAddr: String,
  smtpFromName: String
)
/*
object SmtpSettings {
  def apply(vals: Seq[SystemSetting]) = {
    apply(vals.map(s => (s.key, s.value)).toMap)
  }
  def apply(vals: Map[String, SystemSetting]) = {
    try {
      Some(new SmtpSettings(
        vals.get("smtpHost").value,
        vals.get("smtpPort").value.toInt,
        vals.get("smtpUser").value,
        vals.get("smtpPassword").value,
        vals.get("smtpSSL").value,
        vals.get("smtpFromAddr").value,
        vals.get("smtpFromName").value
      ))
    } catch {
      case _ => None
    }
  }
}
*/

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
    value: Option[String] = None)(implicit session: DBSession = autoSession): SystemSetting = {
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
