package turqey.utils

import org.scalatest.FunSuite
import turqey.utils.MailUtil._

class MailUtilSpec extends FunSuite {

  test("sendGmail") {

    val setting = SmtpSetting(
      host     = "smtp.gmail.com",
      port     = 587,
      authId   = Some(System.getEnv()),
      authPass = Some(System.getEnv()),
      tls      = true,
      from     = MailAddress(System.getEnv())
    )

    MailUtil.send( Seq(Mail(
      subject  = "testSubject",
      content  = "testContent",
      toAddr   = MailAddress(System.getEnv())
    )) )
    
    assert(java.util.Arrays.equals(bytes, img.binary))
  }

}

