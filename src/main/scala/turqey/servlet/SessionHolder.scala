package turqey.servlet

import javax.servlet._
import javax.servlet.http._
import org.scalatra.servlet._

object SessionHolder {
  val sessionOfThread = new ThreadLocal[HttpSession]

  def get:RichSession = { new RichSession(this.sessionOfThread.get()) }
  def user:Option[UserSession] = { try{ this.get.get("user").asInstanceOf[Option[UserSession]] } catch { case _: Exception => None } }
  def set(session: HttpSession):Unit = { this.sessionOfThread.set(session) }
  def root:Boolean = this.user match { case Some(u) => { u.root } case _ => false }
}

case class UserSession(id: Long, name: String, imgUrl: String, root: Boolean)
