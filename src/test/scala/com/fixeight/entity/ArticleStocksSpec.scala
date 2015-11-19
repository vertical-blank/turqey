package com.fixeight.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._


class ArticleStocksSpec extends Specification {

  "ArticleStocks" should {

    val as = ArticleStocks.syntax("as")

    "find by primary keys" in new AutoRollback {
      val maybeFound = ArticleStocks.find(1L)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = ArticleStocks.findBy(sqls.eq(as.id, 1L))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = ArticleStocks.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = ArticleStocks.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = ArticleStocks.findAllBy(sqls.eq(as.id, 1L))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = ArticleStocks.countBy(sqls.eq(as.id, 1L))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = ArticleStocks.create()
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = ArticleStocks.findAll().head
      // TODO modify something
      val modified = entity
      val updated = ArticleStocks.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = ArticleStocks.findAll().head
      ArticleStocks.destroy(entity)
      val shouldBeNone = ArticleStocks.find(1L)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = ArticleStocks.findAll()
      entities.foreach(e => ArticleStocks.destroy(e))
      val batchInserted = ArticleStocks.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
