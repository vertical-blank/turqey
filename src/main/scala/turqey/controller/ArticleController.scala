package turqey.controller

import org.scalatra._
import javax.servlet.http.HttpServletRequest
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.article._

import scalaz._
import scalaz.Scalaz._

import turqey.utils.Implicits._

class ArticleController extends ControllerBase  {
  override val path = "/article"

  val view = get("/:id"){
    implicit val session = AutoSession

    val articleId = params.getOrElse("id", redirect("/")).parseLong.toOption.getOrElse(redirect("/"))
    val article = Article.find(articleId).getOrElse(redirect("/"))
    val owner = User.find(article.owner).get
    //val latestEdit = ArticleHistory.findAllBy(sql.eq(ArticleHistory.ah.articleId, articleId)).tail
    val taggings = ArticleTagging.findAllBy(sqls.eq(ArticleTagging.at.articleId, articleId))
    // TODO should be refactored as cache.
    val allTags = turqey.entity.Tag.findAll().map( x => (x.id, x) ).toMap
    val tags = taggings.map( x => allTags(x.tagId) )

    html.view(article, owner, tags)
  }

  val edit = get("/:id/edit"){
    implicit val session = AutoSession

    val articleId = params.getOrElse("id", redirect("/")).parseLong.toOption.getOrElse(redirect("/"))
    val article = Article.find(articleId).getOrElse(redirect("/"))
    val taggings = ArticleTagging.findAllBy(sqls.eq(ArticleTagging.at.articleId, articleId))
    // TODO should be refactored as cache.
    val allTags = turqey.entity.Tag.findAll().map( x => (x.id, x) ).toMap
    val tags = taggings.map( x => allTags(x.tagId) )

    html.edit(Some(article), tags)
  }

  val history = get("/:id/history"){
    implicit val session = AutoSession

    val articleId = params.getOrElse("id", redirect("/")).parseLong.toOption.getOrElse(redirect("/"))

    val article = Article.find(articleId).getOrElse(redirect("/"))

    val histories = ArticleHistory.findAll()

    html.history(article, histories)
  }

  post("/:id"){
    implicit val session = AutoSession

    val articleId = params.getOrElse("id", "").parseLong.toOption
    val title     = params.getOrElse("title", "").toString
    val content   = params.getOrElse("content", "").toString
    val tagIds    = multiParams("tagIds")
    val tagNames  = multiParams("tagNames")

    articleId.foreach { articleId => {
        val article = Article.find(articleId).getOrElse(redirect("/"))

        val diff = turqey.utils.DiffUtil.uniDiff(article.content, content).mkString("\r\n")

        article.copy(
          title   = title,
          content = content
        ).save()

        ArticleHistory.create(
          articleId = articleId,
          diff      = diff,
          userId    = Some(turqey.servlet.SessionHolder.user.get.id)
        )

        tagIds.zip(tagNames).foreach { pair =>
          // foreach entry that id is null, search db by name
          // then there are no matches, insert into tags and get newid.
          // if id is not null, then use it.
          // search articleTaggings by new tag-ids, delete or insert.
          println (pair)
        }
      }
    }
    
    redirect(url(view, "id" -> articleId.get.toString))
  }

  val newEdit = get("/edit"){
    html.edit(None, Seq())
  }

  post("/"){
    implicit val session = AutoSession

    val title     = params.getOrElse("title", "").toString
    val content   = params.getOrElse("content", "").toString

    val newId = Article.create(
      title   = title,
      content = content,
      owner   = turqey.servlet.SessionHolder.user.get.id
    ).id
    
    redirect(url(view, "id" -> newId.toString))
  }

}

