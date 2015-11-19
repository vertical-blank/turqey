package turqey.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._


class GroupsSpec extends Specification {

  "Groups" should {

    val g = Groups.syntax("g")

    "find by primary keys" in new AutoRollback {
      val maybeFound = Groups.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = Groups.findBy(sqls.eq(g.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = Groups.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = Groups.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = Groups.findAllBy(sqls.eq(g.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = Groups.countBy(sqls.eq(g.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = Groups.create()
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = Groups.findAll().head
      // TODO modify something
      val modified = entity
      val updated = Groups.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = Groups.findAll().head
      Groups.destroy(entity)
      val shouldBeNone = Groups.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = Groups.findAll()
      entities.foreach(e => Groups.destroy(e))
      val batchInserted = Groups.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
