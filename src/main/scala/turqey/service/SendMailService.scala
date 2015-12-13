package turqey.service


case class Mail(fomrAddr: String, toAddr: Seq[String]);

object Mailer {

  def sendAllNotifications() = {
    val stockNotifs = getStockNotifications()
    val commentNotifs = getCommentNotifications()

    //val notifsByToAddr = 

  }

  def getStockNotifications(): Seq[Mail] = {
    Seq()
  }

  def getCommentNotifications(): Seq[Mail] = {
    Seq()
  }

  def send(mails: Map[String, Seq[Mail]]) = {
    // connect

    // send mails

    // close

  }

}

