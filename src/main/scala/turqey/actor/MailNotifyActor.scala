
package turqey.actor

import akka.actor.Actor

import javax.mail._
import javax.mail.internet._

case class Smtp(host: String, port: Int, tls: Boolean, auth: Boolean, mailAddress: Option[String], password: Option[String]){
  def validate(): Option[Smtp] = {
    if (auth && (!mailAddress.isDefined || !password.isDefined)){
      None
    } else {
      Some(this)
    }
  }
}

object Smtp {
  var property: Option[java.util.Properties] = None
  var auth: Option[PasswordAuthentication] = None
  
  def set(
    host: String,
    port: Int,
    tls: Boolean = false,
    auth: Boolean = false,
    mailAddress: Option[String] = None,
    password: Option[String] = None) = {
    val instance = new Smtp(host, port, auth, tls, mailAddress, password)
    instance.validate() match {
      case Some(x) => {
        val property = new java.util.Properties()
        property.put("mail.smtp.host",            host)
        property.put("mail.smtp.port",            port.toString)
        property.put("mail.smtp.auth",            auth.toString)
        property.put("mail.smtp.starttls.enable", tls.toString)
        this.property = Some(property)
        this.auth = Some(new PasswordAuthentication("", "")) 
      }
      case _ => {
        this.property = None
        this.auth = None
      }
    }
  }

  def getProperty(): Option[java.util.Properties] = { property }
  def getAuthentication(): Option[PasswordAuthentication] = { auth }
}

class MailNotificationActor extends Actor {
  def receive = {
    case x:String => {

      val property = new java.util.Properties()
      property.put("mail.smtp.host",            "smtp.gmail.com")
      property.put("mail.smtp.port",            "587")
      property.put("mail.smtp.auth",            "true")
      property.put("mail.smtp.starttls.enable", "true")

      val session = Session.getInstance(property, new javax.mail.Authenticator(){
        override def getPasswordAuthentication(): PasswordAuthentication = {
          new PasswordAuthentication("", "")
        }
      })

      val mimeMessage = new MimeMessage(session)

      val toAddr   = new InternetAddress("", "")
      val fromAddr = new InternetAddress("noreply@casleyconsulting.co.jp", "Turqey@Casley");
      mimeMessage.setFrom(fromAddr);
      mimeMessage.setRecipient(Message.RecipientType.TO, toAddr)
      mimeMessage.setSubject("title", "ISO-2022-JP");
      mimeMessage.setText("message", "ISO-2022-JP");

      Transport.send(mimeMessage)
    }
  }
}


