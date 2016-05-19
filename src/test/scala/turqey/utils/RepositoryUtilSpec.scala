package turqey.utils

import org.scalatest.FunSuite
import scala.concurrent.Future

class RepositoryUtilSpec extends FunSuite {
  test("saveAsMaster") {
    val repo = new WritableArticleRepository(-1, Ident(-99, "user.name", "user.email"))
    
    repo.master.save(
      "title",
      "content",
      Seq(1,2,3),
      Seq()
    )

    val article = repo.master.headArticle

    assert(article.title == "title")
    assert(article.content == "content")
  }

  test("saveAsDraft") {
    val repo = new WritableArticleRepository(-2, Ident(-99, "user.name", "user.email"))
    
    repo.draft.save(
      "title",
      "content",
      Seq(1,2,3),
      Seq()
    )

    val article = repo.draft.headArticle
    assert(article.title == "title")
    assert(article.content == "content")
  }
  
}

