package turqey.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._


class TagsSpec extends Specification {

  "Tags" should {

    val t = Tags.syntax("t")

    "find by primary keys" in new AutoRollback {
      val maybeFound = Tags.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = Tags.findBy(sqls.eq(t.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = Tags.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = Tags.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = Tags.findAllBy(sqls.eq(t.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = Tags.countBy(sqls.eq(t.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = Tags.create()
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = Tags.findAll().head
      // TODO modify something
      val modified = entity
      val updated = Tags.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = Tags.findAll().head
      Tags.destroy(entity)
      val shouldBeNone = Tags.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = Tags.findAll()
      entities.foreach(e => Tags.destroy(e))
      val batchInserted = Tags.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
