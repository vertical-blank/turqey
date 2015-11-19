package turqey.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class ArticleCommentSpec extends Specification {

  "ArticleComment" should {

    val ac = ArticleComment.syntax("ac")

    "find by primary keys" in new AutoRollback {
      val maybeFound = ArticleComment.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = ArticleComment.findBy(sqls.eq(ac.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = ArticleComment.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = ArticleComment.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = ArticleComment.findAllBy(sqls.eq(ac.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = ArticleComment.countBy(sqls.eq(ac.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = ArticleComment.create(articleId = 1L, userId = 1L, content = null)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = ArticleComment.findAll().head
      // TODO modify something
      val modified = entity
      val updated = ArticleComment.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = ArticleComment.findAll().head
      ArticleComment.destroy(entity)
      val shouldBeNone = ArticleComment.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = ArticleComment.findAll()
      entities.foreach(e => ArticleComment.destroy(e))
      val batchInserted = ArticleComment.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
