package db.migration

import org.flywaydb.core.api.migration.jdbc.JdbcMigration
import java.sql.Connection
import scalikejdbc._
import turqey.entity.{Article, ArticleHistory, ArticleTagging, User}
import turqey.utils.Implicits._
import turqey.utils.{ArticleRepository, Ident}

class V1_8__ArticleToGit extends JdbcMigration {

  override def migrate(conn: Connection):Unit = {
    val db = ThreadLocalDB.create(conn)

    ThreadLocalDB.load() withinTx { implicit session =>
      val tagIds = ArticleTagging.findAll().map( at => (at.articleId, at.tagId) ).groupBy(_._1)
      val idents = User.findAll().map( u => (u.id, Ident(u.id, u.name, u.email)) ).toMap
      Article.findAll().foreach ( a => {
        val headCommit = new ArticleRepository(a.id, Some(idents(a.ownerId))).master.save(
          a.title,
          a.content,
          tagIds.getOrElse(a.id, Seq[(Long, Long)]()).map( _._2 ),
          Seq()
        )
        a.copy( published = true ).save()
        ArticleHistory.create(a.id, headCommit.getId, Some(a.ownerId), a.created)
      })
    }

  }
}


