package turqey.servlet

import javax.servlet._
import scalikejdbc._
import scalikejdbc.config._
import org.flywaydb.core._
import turqey.entity.User
import turqey.utils.Digest

class InitListener extends ServletContextListener {

  override def contextInitialized(event: ServletContextEvent):Unit = {
    DBs.setupAll()

    val flyway = new Flyway()
    flyway.setDataSource(ConnectionPool.dataSource())
    flyway.migrate()
    
    if (User.countAll() == 0){
      User.create(
        email    = "root",
        name     = "root",
        password = Some(Digest.get("root")),
        imgUrl   = "",
        root     = false
      )
    }
    
    ServletContextHolder.init(event.getServletContext)
  }

  override def contextDestroyed(event: ServletContextEvent):Unit = {
    DBs.closeAll()
  }

}

case class ServletContextHolder(srvCtx: ServletContext)
  
object ServletContextHolder {
  var srvCxt:ServletContext = null

  def init(instance: ServletContext): Unit = { this.srvCxt = instance }

  def get():ServletContext = { this.srvCxt } 
  def root:String = { this.srvCxt.getContextPath }
  def assets:String = { this.root + "/assets" }
}

