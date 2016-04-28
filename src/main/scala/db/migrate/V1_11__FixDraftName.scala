package db.migration

import org.flywaydb.core.api.migration.jdbc.JdbcMigration
import java.sql.Connection
import scalikejdbc._
import turqey.entity.Draft
import turqey.utils.Implicits._
import turqey.utils.{RepositoryUtil, Ident}
import turqey.utils.RepositoryUtil._

class V1_11__FixDraftName extends JdbcMigration {

  override def migrate(conn: Connection):Unit = {
    val db = ThreadLocalDB.create(conn)

    ThreadLocalDB.load() withinTx { implicit session =>
      Draft.findAll.foreach { d =>
        RepositoryUtil.getArticleRepo(d.articleId).branch("draft").createNewBranch("draftOf" + d.ownerId.toString)
      }
    }
  }
}


