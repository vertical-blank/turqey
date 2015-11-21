package turqey.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._


class ProjectSpec extends Specification {

  "Project" should {

    val p = Project.syntax("p")

    "find by primary keys" in new AutoRollback {
      val maybeFound = Project.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = Project.findBy(sqls.eq(p.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = Project.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = Project.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = Project.findAllBy(sqls.eq(p.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = Project.countBy(sqls.eq(p.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = Project.create(name = "MyString", owner = 1L)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = Project.findAll().head
      // TODO modify something
      val modified = entity
      val updated = Project.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = Project.findAll().head
      Project.destroy(entity)
      val shouldBeNone = Project.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = Project.findAll()
      entities.foreach(e => Project.destroy(e))
      val batchInserted = Project.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
