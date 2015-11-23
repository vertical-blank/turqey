package turqey.controller

import org.scalatra._
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.article._

import turqey.utils.Implicits._

class ArticleController extends ControllerBase  {
  override val path = "/article"

  val view = get("/:id"){
    val userId    = turqey.servlet.SessionHolder.user.get.id

    val articleId = params.getOrElse("id", redirect("/")).toLong
    val article = Article.find(articleId).getOrElse(redirect("/"))
    //val latestEdit = ArticleHistory.findAllBy(sql.eq(ArticleHistory.ah.articleId, articleId)).tail
    val taggings = ArticleTagging.findAllBy(sqls.eq(ArticleTagging.at.articleId, articleId))
    // TODO should be refactored as cache.
    val allTags = turqey.entity.Tag.findAll().map( x => (x.id, x) ).toMap
    val tags = taggings.map( x => allTags(x.tagId) )
    val comments = ArticleComment.findAllBy(sqls.eq(ArticleComment.ac.articleId, articleId))
    val as = ArticleStock.as
    val stocked = ArticleStock.findBy(sqls
      .eq(as.articleId, articleId).and
      .eq(as.userId, userId)
    ).isDefined

    html.view(article, tags, comments, stocked)
  }

  val edit = get("/:id/edit"){
    val articleId = params.getOrElse("id", redirect("/")).toLong
    val article = Article.find(articleId).getOrElse(redirect("/"))
    if (!article.editable) { redirect("/") }

    val taggings = ArticleTagging.findAllBy(sqls.eq(ArticleTagging.at.articleId, articleId))
    // TODO should be refactored as cache.
    val allTags = Tag.findAll().map( x => (x.id, x) ).toMap
    val tags = taggings.map( x => allTags(x.tagId) )

    html.edit(Some(article), tags)
  }

  val history = get("/:id/history"){
    val articleId = params.getOrElse("id", redirect("/")).toLong

    val article = Article.find(articleId).getOrElse(redirect("/"))

    val histories = ArticleHistory.findAll()

    html.history(article, histories)
  }

  post("/:id/comment"){
    val articleId = params.getOrElse("id", redirect("/")).toLong
    val comment   = params.getOrElse("comment", "").toString
    
    params.get("commentId") match {
      case Some(commentId) => {
        ArticleComment.find(commentId.toLong).get.copy(
          content = comment
        ).save()
      }
      case _ => {
        ArticleComment.create(
          articleId = articleId,
          content   = comment,
          userId    = turqey.servlet.SessionHolder.user.get.id
        )
      }
    }

    redirect(url(view, "id" -> articleId.toString))
  }

  post("/:id"){
    val articleId = params.getOrElse("id", "").toLong
    val title     = params.getOrElse("title", "").toString
    val content   = params.getOrElse("content", "").toString
    val tagIds    = multiParams("tagIds")
    val tagNames  = multiParams("tagNames")

    val article = Article.find(articleId).getOrElse(redirect("/"))
    if (!article.editable) { redirect("/") }
    val oldContent = Implicits.clobToString(article.content)

    article.copy(
      title   = title,
      content = content
    ).save()

    val diff = turqey.utils.DiffUtil.uniDiff(oldContent, content).mkString("\r\n")
    ArticleHistory.create(
      articleId = articleId,
      diff      = diff,
      userId    = Some(turqey.servlet.SessionHolder.user.get.id)
    )

    refreshTaggings(articleId, tagIds, tagNames);
    
    redirect(url(view, "id" -> articleId.toString))
  }

  val newEdit = get("/edit"){
    html.edit(None, Seq())
  }

  post("/"){
    val title     = params.getOrElse("title", "").toString
    val content   = params.getOrElse("content", "").toString
    val tagIds    = multiParams("tagIds")
    val tagNames  = multiParams("tagNames")

    val newId = Article.create(
      title   = title,
      content = content,
      owner   = turqey.servlet.SessionHolder.user.get.id
    ).id

    refreshTaggings(newId, tagIds, tagNames);
    
    redirect(url(view, "id" -> newId.toString))
  }
  
  val stock = post("/:id/stock"){
    val articleId = params.getOrElse("id", redirect("/")).toLong
    val userId    = turqey.servlet.SessionHolder.user.get.id
    val as = ArticleStock.as
    ArticleStock.findBy(sqls
      .eq(as.articleId, articleId).and
      .eq(as.userId, userId)
    ) match {
      case Some(a)  => {
        ArticleStock.destroy(a)
      }
      case None     => {
        ArticleStock.create(
          articleId = articleId,
          userId    = userId
        )
      }
    }
  }

  private def refreshTaggings(articleId: Long, tagIds: Seq[String], tagNames: Seq[String]): Unit = {
    val tagIdName = tagIds.zip(tagNames).map { case (id, name) =>
      (id, name) match {
        case ("", _) => {
          val tag = Tag.findBy(sqls.eq(Tag.t.name, name)).getOrElse(
            Tag.create( name = name )
          )
          (tag.id, tag.name)
        }
        case _ => (id.toLong, name)
      }
    }

    val tagIdsOld = ArticleTagging.findAllBy(sqls.eq(ArticleTagging.at.articleId, articleId)).map( x => x.tagId )
    val tagIdsNew = tagIdName.map { case (id, name) => id }

    val tagIdsForDelete = tagIdsOld diff tagIdsNew
    val tagIdsForInsert = tagIdsNew diff tagIdsOld

    ArticleTagging.deleteTagsOfArticle(articleId, tagIdsForDelete)
    ArticleTagging.insertTagsOfArticle(articleId, tagIdsForInsert)
  }

}

