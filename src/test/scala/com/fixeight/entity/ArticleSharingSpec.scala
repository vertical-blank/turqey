package com.fixeight.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._


class ArticleSharingSpec extends Specification {

  "ArticleSharing" should {

    val as = ArticleSharing.syntax("as")

    "find by primary keys" in new AutoRollback {
      val maybeFound = ArticleSharing.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = ArticleSharing.findBy(sqls.eq(as.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = ArticleSharing.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = ArticleSharing.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = ArticleSharing.findAllBy(sqls.eq(as.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = ArticleSharing.countBy(sqls.eq(as.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = ArticleSharing.create(parentId = 1L)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = ArticleSharing.findAll().head
      // TODO modify something
      val modified = entity
      val updated = ArticleSharing.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = ArticleSharing.findAll().head
      ArticleSharing.destroy(entity)
      val shouldBeNone = ArticleSharing.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = ArticleSharing.findAll()
      entities.foreach(e => ArticleSharing.destroy(e))
      val batchInserted = ArticleSharing.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
