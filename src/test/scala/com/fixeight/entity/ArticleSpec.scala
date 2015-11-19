package com.fixeight.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class ArticleSpec extends Specification {

  "Article" should {

    val a = Article.syntax("a")

    "find by primary keys" in new AutoRollback {
      val maybeFound = Article.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = Article.findBy(sqls.eq(a.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = Article.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = Article.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = Article.findAllBy(sqls.eq(a.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = Article.countBy(sqls.eq(a.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = Article.create(title = "MyString", content = null, owner = 1L)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = Article.findAll().head
      // TODO modify something
      val modified = entity
      val updated = Article.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = Article.findAll().head
      Article.destroy(entity)
      val shouldBeNone = Article.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = Article.findAll()
      entities.foreach(e => Article.destroy(e))
      val batchInserted = Article.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
