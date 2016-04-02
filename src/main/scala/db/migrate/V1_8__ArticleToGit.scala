package db.migration

import org.flywaydb.core.api.migration.jdbc.JdbcMigration
import java.sql.Connection
import scalikejdbc._
import turqey.entity.{Article, ArticleTagging, User}
import turqey.utils.Implicits._
import turqey.utils.{RepositoryUtil, Ident}
import turqey.utils.RepositoryUtil._

class V1_8__ArticleToGit extends JdbcMigration {

  override def migrate(conn: Connection):Unit = {
    val db = ThreadLocalDB.create(conn)

    ThreadLocalDB.load() withinTx { implicit session =>
      val tagIds = ArticleTagging.findAll().map( at => (at.articleId, at.tagId) ).groupBy(_._1)
      val idents = User.findAll().map( u => (u.id, Ident(u.name, u.email))).toMap
      Article.findAll().foreach ( a => {
        RepositoryUtil.saveAsMaster(a.id, a.title, a.content, tagIds.getOrElse(a.id, Seq[(Long, Long)]()).map( _._2 ), idents(a.ownerId))
        a.copy( published = true ).save()
      })
    }

  }
}


