package com.fixeight.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._


class GroupMembersSpec extends Specification {

  "GroupMembers" should {

    val gm = GroupMembers.syntax("gm")

    "find by primary keys" in new AutoRollback {
      val maybeFound = GroupMembers.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = GroupMembers.findBy(sqls.eq(gm.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = GroupMembers.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = GroupMembers.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = GroupMembers.findAllBy(sqls.eq(gm.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = GroupMembers.countBy(sqls.eq(gm.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = GroupMembers.create(groupId = 1L)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = GroupMembers.findAll().head
      // TODO modify something
      val modified = entity
      val updated = GroupMembers.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = GroupMembers.findAll().head
      GroupMembers.destroy(entity)
      val shouldBeNone = GroupMembers.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = GroupMembers.findAll()
      entities.foreach(e => GroupMembers.destroy(e))
      val batchInserted = GroupMembers.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
