package turqey.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class UserSpec extends Specification {

  "User" should {

    val u = User.syntax("u")

    "find by primary keys" in new AutoRollback {
      val maybeFound = User.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = User.findBy(sqls.eq(u.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = User.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = User.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = User.findAllBy(sqls.eq(u.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = User.countBy(sqls.eq(u.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = User.create()
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = User.findAll().head
      // TODO modify something
      val modified = entity
      val updated = User.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = User.findAll().head
      User.destroy(entity)
      val shouldBeNone = User.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = User.findAll()
      entities.foreach(e => User.destroy(e))
      val batchInserted = User.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
