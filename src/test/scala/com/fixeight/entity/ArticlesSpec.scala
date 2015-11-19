package com.fixeight.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class ArticlesSpec extends Specification {

  "Articles" should {

    val a = Articles.syntax("a")

    "find by primary keys" in new AutoRollback {
      val maybeFound = Articles.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = Articles.findBy(sqls.eq(a.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = Articles.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = Articles.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = Articles.findAllBy(sqls.eq(a.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = Articles.countBy(sqls.eq(a.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = Articles.create()
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = Articles.findAll().head
      // TODO modify something
      val modified = entity
      val updated = Articles.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = Articles.findAll().head
      Articles.destroy(entity)
      val shouldBeNone = Articles.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = Articles.findAll()
      entities.foreach(e => Articles.destroy(e))
      val batchInserted = Articles.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
