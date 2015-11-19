package com.fixeight.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._


class ProjectsSpec extends Specification {

  "Projects" should {

    val p = Projects.syntax("p")

    "find by primary keys" in new AutoRollback {
      val maybeFound = Projects.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = Projects.findBy(sqls.eq(p.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = Projects.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = Projects.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = Projects.findAllBy(sqls.eq(p.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = Projects.countBy(sqls.eq(p.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = Projects.create()
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = Projects.findAll().head
      // TODO modify something
      val modified = entity
      val updated = Projects.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = Projects.findAll().head
      Projects.destroy(entity)
      val shouldBeNone = Projects.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = Projects.findAll()
      entities.foreach(e => Projects.destroy(e))
      val batchInserted = Projects.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
