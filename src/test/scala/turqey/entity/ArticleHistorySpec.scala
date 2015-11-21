package turqey.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class ArticleHistorySpec extends Specification {

  "ArticleHistory" should {

    val ah = ArticleHistory.syntax("ah")

    "find by primary keys" in new AutoRollback {
      val maybeFound = ArticleHistory.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = ArticleHistory.findBy(sqls.eq(ah.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = ArticleHistory.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = ArticleHistory.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = ArticleHistory.findAllBy(sqls.eq(ah.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = ArticleHistory.countBy(sqls.eq(ah.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = ArticleHistory.create(articleId = 1L, diff = null, created = DateTime.now)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = ArticleHistory.findAll().head
      // TODO modify something
      val modified = entity
      val updated = ArticleHistory.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = ArticleHistory.findAll().head
      ArticleHistory.destroy(entity)
      val shouldBeNone = ArticleHistory.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = ArticleHistory.findAll()
      entities.foreach(e => ArticleHistory.destroy(e))
      val batchInserted = ArticleHistory.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
