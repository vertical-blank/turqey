package com.fixeight.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class ArticleHistoriesSpec extends Specification {

  "ArticleHistories" should {

    val ah = ArticleHistories.syntax("ah")

    "find by primary keys" in new AutoRollback {
      val maybeFound = ArticleHistories.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = ArticleHistories.findBy(sqls.eq(ah.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = ArticleHistories.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = ArticleHistories.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = ArticleHistories.findAllBy(sqls.eq(ah.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = ArticleHistories.countBy(sqls.eq(ah.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = ArticleHistories.create()
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = ArticleHistories.findAll().head
      // TODO modify something
      val modified = entity
      val updated = ArticleHistories.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = ArticleHistories.findAll().head
      ArticleHistories.destroy(entity)
      val shouldBeNone = ArticleHistories.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = ArticleHistories.findAll()
      entities.foreach(e => ArticleHistories.destroy(e))
      val batchInserted = ArticleHistories.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
