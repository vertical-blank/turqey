package com.fixeight.servlet

import javax.servlet._
import javax.servlet.http._
import org.scalatra.servlet._
import scalikejdbc._
import scalikejdbc.config._
import org.flywaydb.core._

class RequestListener extends ServletRequestListener {

  override def requestInitialized(event: ServletRequestEvent):Unit = {
    //val session = event.getServletRequest.asInstanceOf[HttpServletRequest].getSession(true)
    //SessionHolder.set(session)
  }

  override def requestDestroyed(event: ServletRequestEvent):Unit = {
  }

}

object SessionHolder {
  val sessionOfThread = new ThreadLocal[HttpSession]

  def get:RichSession = { new RichSession(this.sessionOfThread.get()) }
  def user:Option[UserSession] = { this.get.get("user").asInstanceOf[Option[UserSession]] }
  def set(session: HttpSession):Unit = { this.sessionOfThread.set(session) }
}

case class UserSession(id: Long, name: String, imgUrl: String)


