package com.fixeight.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._


class ArticleSharingsSpec extends Specification {

  "ArticleSharings" should {

    val as = ArticleSharings.syntax("as")

    "find by primary keys" in new AutoRollback {
      val maybeFound = ArticleSharings.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = ArticleSharings.findBy(sqls.eq(as.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = ArticleSharings.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = ArticleSharings.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = ArticleSharings.findAllBy(sqls.eq(as.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = ArticleSharings.countBy(sqls.eq(as.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = ArticleSharings.create()
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = ArticleSharings.findAll().head
      // TODO modify something
      val modified = entity
      val updated = ArticleSharings.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = ArticleSharings.findAll().head
      ArticleSharings.destroy(entity)
      val shouldBeNone = ArticleSharings.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = ArticleSharings.findAll()
      entities.foreach(e => ArticleSharings.destroy(e))
      val batchInserted = ArticleSharings.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
