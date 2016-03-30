package turqey.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class DraftSpec extends Specification {

  "Draft" should {

    val d = Draft.syntax("d")

    "find by primary keys" in new AutoRollback {
      val maybeFound = Draft.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = Draft.findBy(sqls.eq(d.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = Draft.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = Draft.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = Draft.findAllBy(sqls.eq(d.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = Draft.countBy(sqls.eq(d.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = Draft.create(title = "MyString", content = null, ownerId = 1L, created = DateTime.now)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = Draft.findAll().head
      // TODO modify something
      val modified = entity
      val updated = Draft.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = Draft.findAll().head
      Draft.destroy(entity)
      val shouldBeNone = Draft.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = Draft.findAll()
      entities.foreach(e => Draft.destroy(e))
      val batchInserted = Draft.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
