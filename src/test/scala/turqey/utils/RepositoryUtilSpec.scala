package turqey.utils

import org.scalatest.FunSuite
import scala.concurrent.Future

class RepositoryUtilSpec extends FunSuite {
  import glitch.GitRepository.Ident

  test("saveAsMaster") {
    RepositoryUtil.saveAsMaster(
      -1,
      "title",
      "content",
      Seq(1,2,3),
      new Ident("user.name", "user.email"),
      Seq()
    )

    val article = RepositoryUtil.headArticle(-1, "master")

    assert(article.title == "title")
    assert(article.content == "content")
  }

  test("saveAsDraft") {
    RepositoryUtil.saveAsDraft(
      -2,
      "title",
      "content",
      Seq(1,2,3),
      new Ident("user.name", "user.email"),
      Seq()
    )

    val article = RepositoryUtil.headArticle(-2, "draft")

    assert(article.title == "title")
    assert(article.content == "content")
  }
  
}

