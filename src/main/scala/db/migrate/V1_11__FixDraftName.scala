package db.migration

import org.flywaydb.core.api.migration.jdbc.JdbcMigration
import java.sql.Connection
import scalikejdbc._
import turqey.entity.{Draft, User}
import turqey.utils.Implicits._
import turqey.utils.{WritableArticleRepository, Ident}

class V1_11__FixDraftName extends JdbcMigration {

  override def migrate(conn: Connection):Unit = {
    val db = ThreadLocalDB.create(conn)

    ThreadLocalDB.load() withinTx { implicit session =>
      Draft.findAll.foreach { d =>
        val u = User.find(d.ownerId).get
        val branch = new WritableArticleRepository(d.articleId, Ident(u.id, u.name, u.email)).draft
        if (branch.exists)
          branch.createNewBranch("draftOf" + d.ownerId.toString)
      }
    }
  }
}


