package turqey.utils

import javax.mail.internet._
import javax.mail._

case class SmtpSetting(
  host:     Option[String] = None,
  port:     Option[Integer] = None,
  authId:   Option[String] = None,
  authPass: Option[String] = None,
  tls:      Option[Boolean] = None,
  from:     Option[MailAddress] = None
) {
  val auth = authId.zip(authPass) nonEmpty
  def isPresent = host.zip(tls).zip(from) nonEmpty
}

case class Mail(
  subject:  String,
  content:  String,
  toAddr:   MailAddress
)

case class MailAddress(
  address:  String = "",
  name:     Option[String] = None
) { def toInternetAddress = new InternetAddress(address, name.getOrElse(address)) }

object MailUtil {

  var setting: SmtpSetting = loadSetting()

  def settingFile = new java.io.File(FileUtil.homeDir, "smtp.json")

  def loadSetting(): SmtpSetting = {
    try {
      FileUtil.readJsonFileAs[SmtpSetting](this.settingFile)
    } catch {
      case _: Throwable => SmtpSetting()
    }
  }

  def saveSetting(setting: SmtpSetting): Unit = {
    FileUtil.writeText(Json.toJson(setting), this.settingFile)
    this.setting = setting
  }

  def send(mails: Seq[Mail]): Unit = {

    if (!setting.isPresent) {
      return
    }
    
    val sendDate = new java.util.Date()

    val session = {
      val properties = new java.util.Properties();
      properties.put("mail.smtp.host", setting.host)
      properties.put("mail.smtp.port", setting.port)
      properties.put("mail.smtp.auth", setting.auth.toString)
      properties.put("mail.smtp.starttls.enable", setting.tls.toString)
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
          message.setFrom(setting.from.get.toInternetAddress)
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

