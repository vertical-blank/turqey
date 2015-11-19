package turqey.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class ArticleCommentsSpec extends Specification {

  "ArticleComments" should {

    val ac = ArticleComments.syntax("ac")

    "find by primary keys" in new AutoRollback {
      val maybeFound = ArticleComments.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = ArticleComments.findBy(sqls.eq(ac.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = ArticleComments.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = ArticleComments.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = ArticleComments.findAllBy(sqls.eq(ac.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = ArticleComments.countBy(sqls.eq(ac.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = ArticleComments.create()
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = ArticleComments.findAll().head
      // TODO modify something
      val modified = entity
      val updated = ArticleComments.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = ArticleComments.findAll().head
      ArticleComments.destroy(entity)
      val shouldBeNone = ArticleComments.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = ArticleComments.findAll()
      entities.foreach(e => ArticleComments.destroy(e))
      val batchInserted = ArticleComments.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
