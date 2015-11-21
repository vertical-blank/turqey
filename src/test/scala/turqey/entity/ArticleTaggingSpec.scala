package turqey.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class ArticleTaggingSpec extends Specification {

  "ArticleTagging" should {

    val at = ArticleTagging.syntax("at")

    "find by primary keys" in new AutoRollback {
      val maybeFound = ArticleTagging.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = ArticleTagging.findBy(sqls.eq(at.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = ArticleTagging.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = ArticleTagging.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = ArticleTagging.findAllBy(sqls.eq(at.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = ArticleTagging.countBy(sqls.eq(at.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = ArticleTagging.create(articleId = 1L, tagId = 1L, created = DateTime.now)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = ArticleTagging.findAll().head
      // TODO modify something
      val modified = entity
      val updated = ArticleTagging.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = ArticleTagging.findAll().head
      ArticleTagging.destroy(entity)
      val shouldBeNone = ArticleTagging.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = ArticleTagging.findAll()
      entities.foreach(e => ArticleTagging.destroy(e))
      val batchInserted = ArticleTagging.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
