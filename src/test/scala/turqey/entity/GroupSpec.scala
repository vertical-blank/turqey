package turqey.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._


class GroupSpec extends Specification {

  "Group" should {

    val g = Group.syntax("g")

    "find by primary keys" in new AutoRollback {
      val maybeFound = Group.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = Group.findBy(sqls.eq(g.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = Group.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = Group.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = Group.findAllBy(sqls.eq(g.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = Group.countBy(sqls.eq(g.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = Group.create(name = "MyString", owner = 1L)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = Group.findAll().head
      // TODO modify something
      val modified = entity
      val updated = Group.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = Group.findAll().head
      Group.destroy(entity)
      val shouldBeNone = Group.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = Group.findAll()
      entities.foreach(e => Group.destroy(e))
      val batchInserted = Group.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
