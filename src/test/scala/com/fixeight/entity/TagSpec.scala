package com.fixeight.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._


class TagSpec extends Specification {

  "Tag" should {

    val t = Tag.syntax("t")

    "find by primary keys" in new AutoRollback {
      val maybeFound = Tag.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = Tag.findBy(sqls.eq(t.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = Tag.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = Tag.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = Tag.findAllBy(sqls.eq(t.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = Tag.countBy(sqls.eq(t.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = Tag.create(name = "MyString")
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = Tag.findAll().head
      // TODO modify something
      val modified = entity
      val updated = Tag.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = Tag.findAll().head
      Tag.destroy(entity)
      val shouldBeNone = Tag.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = Tag.findAll()
      entities.foreach(e => Tag.destroy(e))
      val batchInserted = Tag.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
