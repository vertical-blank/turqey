package turqey.servlet

import javax.servlet._
import javax.servlet.http._
import org.scalatra.servlet._

object SessionHolder {
  val sessionOfThread = new ThreadLocal[HttpSession]

  def get:RichSession = { new RichSession(this.sessionOfThread.get()) }
  def user:Option[UserSession] = { this.get.get("user").asInstanceOf[Option[UserSession]] }
  def set(session: HttpSession):Unit = { this.sessionOfThread.set(session) }
}

case class UserSession(id: Long, name: String, imgUrl: String, root: Boolean)


