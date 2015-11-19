package com.fixeight.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._


class ArticleStockSpec extends Specification {

  "ArticleStock" should {

    val as = ArticleStock.syntax("as")

    "find by primary keys" in new AutoRollback {
      val maybeFound = ArticleStock.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = ArticleStock.findBy(sqls.eq(as.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = ArticleStock.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = ArticleStock.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = ArticleStock.findAllBy(sqls.eq(as.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = ArticleStock.countBy(sqls.eq(as.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = ArticleStock.create(articleId = 1L, userId = 1L)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = ArticleStock.findAll().head
      // TODO modify something
      val modified = entity
      val updated = ArticleStock.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = ArticleStock.findAll().head
      ArticleStock.destroy(entity)
      val shouldBeNone = ArticleStock.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = ArticleStock.findAll()
      entities.foreach(e => ArticleStock.destroy(e))
      val batchInserted = ArticleStock.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
