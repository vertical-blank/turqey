package turqey.utils

import javax.mail.internet._
import javax.mail._

import turqey.entity.SystemSetting
import scalikejdbc._

case class SmtpSetting(
  host:     Option[String],
  port:     Option[Integer],
  authId:   Option[String],
  authPass: Option[String],
  ssl:      Option[Boolean],
  from:     MailAddress
) {
  val auth = authId.zip(authPass) nonEmpty
  def toSeq: Seq[SystemSetting] = Seq(
    host    .map(x => SystemSetting(SmtpSetting.Keys.host,     x)),
    port    .map(x => SystemSetting(SmtpSetting.Keys.port,     x.toString)),
    authId  .map(x => SystemSetting(SmtpSetting.Keys.authId,   x)),
    authPass.map(x => SystemSetting(SmtpSetting.Keys.authPass, x)),
    ssl     .map(x => SystemSetting(SmtpSetting.Keys.ssl,      x.toString)),
    Some(SystemSetting(SmtpSetting.Keys.fromAddr, from.address)),
    from.name.map(SystemSetting(SmtpSetting.Keys.fromName, _))
  ).flatten

  def toOpt = if (host.zip(port) nonEmpty) Some(this) else None
}
object SmtpSetting {
  object Keys {
    val host      = "smtp.host"
    val port      = "smtp.port"
    val authId    = "smtp.auth_id"
    val authPass  = "smtp.auth_pass"
    val ssl       = "smtp.ssl"
    val fromAddr  = "smtp.from_addr"
    val fromName  = "smtp.from_name"
  }
  def apply(vals: Seq[SystemSetting]): SmtpSetting = {
    apply(vals.map(s => (s.key, s.value)).toMap)
  }
  def apply(vals: Map[String, String]): SmtpSetting = SmtpSetting(
    host     = vals.get(Keys.host),
    port     = vals.get(Keys.port).map(_.toInt),
    authId   = vals.get(Keys.authId),
    authPass = vals.get(Keys.authPass),
    ssl      = vals.get(Keys.ssl).map(_.toBoolean),
    from     = MailAddress(
      vals.get(Keys.fromAddr).getOrElse(""),
      vals.get(Keys.fromName)
    )
  )
}

case class Mail(
  subject:  String,
  content:  String,
  toAddr:   MailAddress
)

case class MailAddress(
  address:  String,
  name:     Option[String] = None
) { def toInternetAddress = new InternetAddress(address, name.getOrElse(address)) }

object MailUtil {

  var settingOpt: Option[SmtpSetting] = None

  def loadSetting()(implicit session: DBSession): Option[SmtpSetting] = {
    val settingOpt = SmtpSetting(SystemSetting.findAll).toOpt
    this.settingOpt = settingOpt
    settingOpt
  }

  def saveSetting(setting: SmtpSetting)(implicit session: DBSession): Unit = {
    this.settingOpt = setting.toOpt
    setting.toSeq.foreach (_.save())
  }

  def send(mails: Seq[Mail]): Unit = {

    if (!settingOpt.isDefined) {
      return
    }

    val setting = settingOpt.get
    
    val sendDate = new java.util.Date()

    val session = {
      val properties = new java.util.Properties();
      properties.put("mail.smtp.host", setting.host.get)
      properties.put("mail.smtp.port", setting.port.get)
      properties.put("mail.smtp.auth", setting.auth.toString)
      properties.put("mail.smtp.starttls.enable", setting.ssl.toString)
      Session.getInstance(properties)
    }

    val transport = session.getTransport("smtp")
    if (setting.auth) {
      transport.connect(setting.host.get, setting.authId.get, setting.authPass.get)
    }
    else {
      transport.connect()
    }

    mails.foreach { m =>
      val toAddr: Array[Address] = Array(m.toAddr.toInternetAddress)

      transport.sendMessage({
          val message = new MimeMessage(session)
          message.setFrom(setting.from.toInternetAddress)
          message.setRecipients(Message.RecipientType.TO, toAddr)
          message.setSubject(m.subject)
          message.setText(m.content)
          message.setSentDate(sendDate)
          message
        }, toAddr)
    }

    transport.close()
  }
}

