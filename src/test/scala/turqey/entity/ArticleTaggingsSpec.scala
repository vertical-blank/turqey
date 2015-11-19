package turqey.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class ArticleTaggingsSpec extends Specification {

  "ArticleTaggings" should {

    val at = ArticleTaggings.syntax("at")

    "find by primary keys" in new AutoRollback {
      val maybeFound = ArticleTaggings.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = ArticleTaggings.findBy(sqls.eq(at.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = ArticleTaggings.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = ArticleTaggings.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = ArticleTaggings.findAllBy(sqls.eq(at.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = ArticleTaggings.countBy(sqls.eq(at.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = ArticleTaggings.create()
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = ArticleTaggings.findAll().head
      // TODO modify something
      val modified = entity
      val updated = ArticleTaggings.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = ArticleTaggings.findAll().head
      ArticleTaggings.destroy(entity)
      val shouldBeNone = ArticleTaggings.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = ArticleTaggings.findAll()
      entities.foreach(e => ArticleTaggings.destroy(e))
      val batchInserted = ArticleTaggings.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
