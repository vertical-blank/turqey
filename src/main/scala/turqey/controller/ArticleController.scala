package turqey.controller

import org.scalatra._
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.utils.Json

import turqey.utils.Implicits._

class ArticleController extends AuthedController with ScalateSupport {
  override val path = "article"

  val view = get("/:id"){ implicit dbSession =>
    val userId    = turqey.servlet.SessionHolder.user.get.id

    val articleId = params.getOrElse("id", redirectFatal("/")).toLong
    val article = Article.find(articleId).getOrElse(redirectFatal("/"))
    val latestEdit = ArticleHistory.findLatestsByIds(Seq(articleId)).get(articleId)
    
    val tags = {
      val taggings = ArticleTagging.findAllBy(sqls.eq(ArticleTagging.at.articleId, articleId))
      val allTags = Tag.findAll().map( x => (x.id, x) ).toMap
      taggings.map( x => allTags(x.tagId) )
    }

    val comments = ArticleComment.findAllBy(sqls.eq(ArticleComment.ac.articleId, articleId))
    val stockers = Article.getStockers(articleId)

    val stocked = {
      val as = ArticleStock.as 
      ArticleStock.findBy(sqls
        .eq(as.articleId, articleId).and
        .eq(as.userId, userId)
      ).isDefined
    }
    val count = ArticleStock.countBy(sqls.eq(ArticleStock.as.articleId, articleId))
    
    def repo = RepositoryUtil.getArticleRepo(articleId)
    val master = repo.branch("master")
    
    val content = new String(master.head().getDir().file("Article.md").bytes())
    
    jade("/article/view", 
      "article"    -> article,
      "content"    -> Markdown.html(content),
      "latestEdit" -> latestEdit,
      "tags"       -> tags,
      "comments"   -> comments,
      "stockers"   -> stockers,
      "stocked"    -> stocked,
      "count"      -> count)
  }

  val edit = get("/:id/edit"){ implicit dbSession =>
    val articleId = params.getOrElse("id", redirectFatal("/")).toLong
    val article = Article.find(articleId).getOrElse(redirectFatal("/"))
    if (!article.editable) { redirectFatal("/") }

    val tags = {
      val taggings = ArticleTagging.findAllBy(sqls.eq(ArticleTagging.at.articleId, articleId))
      val allTags = Tag.findAll().map( x => (x.id, x) ).toMap
      taggings.map( x => allTags(x.tagId) )
    }
    
    def repo = RepositoryUtil.getArticleRepo(articleId)
    val master = repo.branch("master")
    
    val content = new String(master.head().getDir().file("Article.md").bytes())

    jade("/article/edit", 
      "article"    -> Some(article),
      "content"    -> content,
      "tags"       -> tags)
  }

  val history = get("/:id/history"){ implicit dbSession =>
    val articleId = params.getOrElse("id", redirectFatal("/")).toLong
    val article = Article.find(articleId).getOrElse(redirectFatal("/"))
    val histories = ArticleHistory.findAll()

    jade("article/history", 
      "article" -> article,
      "histories" -> histories)
  }

// TODO Validate that articleId equals comment.articleId
  post("/:id/comment/:commentId/delete"){ implicit dbSession =>
    val articleId = params.getOrElse("id", redirectFatal("/")).toLong
    val commentId = params.getOrElse("commentId", redirectFatal("/")).toLong
    
    val ac = ArticleComment.ac
    ArticleComment.findBy(sqls.eq(ac.articleId, articleId).and.eq(ac.id, commentId)) match {
      case Some(rec) => { rec.destroy() }
      case _ =>
    }

    redirect(url(view, "id" -> articleId.toString))
  }

  post("/:id/comment"){ implicit dbSession =>
    val articleId = params.getOrElse("id", redirectFatal("/")).toLong
    val comment   = params.getOrElse("comment", "").toString
    var userId    = turqey.servlet.SessionHolder.user.get.id
    
    params.get("commentId") match {
      case Some(commentId) => {
        ArticleComment.find(commentId.toLong).get.copy(
          content = comment
        ).save()
      }
      case _ => {
        val commentId = ArticleComment.create(
          articleId = articleId,
          content   = comment,
          userId    = userId
        ).id
        
        val article = Article.find(articleId)
        
        val commenterIds = ArticleComment.getCommenterIds(articleId).toSet + article.get.ownerId - userId
        
        commenterIds.foreach { c =>
          CommentNotification.create(
            commentId  = commentId,
            notifyToId = c
          )
        }
      }
    }

    redirect(url(view, "id" -> articleId.toString))
  }
  
  post("/draft/"){ implicit dbSession =>
    contentType = "text/json"
    val user = turqey.servlet.SessionHolder.user.get
    
    val content   = params.getOrElse("content", "").toString
    val title     = params.getOrElse("title", "").toString
    val articleId = params.getOrElse("id", {
      Article.create(
        title   = title,
        content = content,
        ownerId = user.id
      ).id.toString
    }).toLong
    
    def repo = RepositoryUtil.getArticleRepo(articleId)
    val draft = {
      val d = repo.branch("draft")
      if (d.exists()) {
        d
      }
      else {
        repo.initialize("initialize as blank", Ident(user))
        repo.branch("master").createNewBranch("draft")
      }
    }
    
    draft.commit(
      new gristle.GitRepository.Dir().put("Article.md", content.getBytes()),
      "%tY/%<tm/%<td %<tH:%<tM:%<tS" format new java.util.Date(),
      Ident(user)
    )
    
    Json.toJson(Map("articleId" -> articleId))
  }

  post("/:id"){ implicit dbSession =>
    val articleId = params.getOrElse("id", "").toLong
    val title     = params.getOrElse("title", "").toString
    val content   = params.getOrElse("content", "").toString
    val tagIds    = multiParams("tagIds")
    val tagNames  = multiParams("tagNames")

    val article = Article.find(articleId).getOrElse(redirectFatal("/"))
    if (!article.editable) { redirectFatal("/") }
    // must read CLOB content before update.
    val oldContent: String = article.content

    article.copy(
      title   = title,
      content = content
    ).save()
    
    def repo = RepositoryUtil.getArticleRepo(articleId)
    val master = repo.branch("master")
    
    val user = turqey.servlet.SessionHolder.user.get
    def ident = Ident(user)
    
    master.commit(
      new gristle.GitRepository.Dir().put("Article.md", content.getBytes()),
      "%tY/%<tm/%<td %<tH:%<tM:%<tS" format new java.util.Date(),
      ident
    )

    val diff = turqey.utils.DiffUtil.uniDiff(oldContent, content).mkString("\r\n")
    ArticleHistory.create(
      articleId = articleId,
      diff      = diff,
      userId    = Some(turqey.servlet.SessionHolder.user.get.id)
    )
    
    Article.getStockers(articleId).foreach { s =>
      ArticleNotification.create(
        articleId  = articleId,
        notifyToId = s.id,
        notifyType = ArticleNotification.TYPES.UPDATE
      )
    }

    refreshTaggings(articleId, tagIds, tagNames);
    
    redirect(url(view, "id" -> articleId.toString))
  }

  post("/:id/delete"){ implicit dbSession =>
    val articleId = params.getOrElse("id", redirectFatal("/")).toLong
    
    Article.find(articleId) match {
      case Some(rec) => rec.destroy()
      case _ =>
    }

    redirect(url("/"))
  }

  val newEdit = get("/edit"){ implicit dbSession =>
    jade("/article/edit", 
      "article"    -> None,
      "content"    -> "",
      "tags"       -> Seq())
  }

  post("/"){ implicit dbSession =>
    val title     = params.getOrElse("title", "").toString
    val content   = params.getOrElse("content", "").toString
    val tagIds    = multiParams("tagIds")
    val tagNames  = multiParams("tagNames")
    
    val user = turqey.servlet.SessionHolder.user.get
    
    val newId = Article.create(
      title   = title,
      content = content,
      ownerId = user.id
    ).id
    
    def repo = RepositoryUtil.getArticleRepo(newId)
    def ident = Ident(user)
    repo.initialize("initial commit", ident)
    val master = repo.branch("master")
    
    master.commit(
      new gristle.GitRepository.Dir().put("Article.md", content.getBytes()),
      "first commit",
      ident
    )
    
    refreshTaggings(newId, tagIds, tagNames)
    
    redirect(url(view, "id" -> newId.toString))
  }
  
  val stock = post("/:id/stock"){ implicit dbSession =>
    contentType = "text/json"

    val articleId = params.getOrElse("id", redirectFatal("/")).toLong
    val userId    = turqey.servlet.SessionHolder.user.get.id
    val as = ArticleStock.as
    ArticleStock.findBy(sqls
      .eq(as.articleId, articleId).and
      .eq(as.userId, userId)
    ) match {
      case Some(a)  => {
        a.destroy()
      }
      case None     => {
        ArticleStock.create(
          articleId = articleId,
          userId    = userId
        )
        StockNotification.create(
          articleId = articleId,
          userId    = userId
        );
      }
    }

    val count = ArticleStock.countBy(sqls.eq(as.articleId, articleId))
    Json.toJson(Map("count" -> count))
  }

  private def refreshTaggings(articleId: Long, tagIds: Seq[String], tagNames: Seq[String])(implicit dbSession: DBSession): Unit = {
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

    val tagIdsForDelete = tagIdsOld diff tagIdsNew distinct
    val tagIdsForInsert = tagIdsNew diff tagIdsOld distinct

    ArticleTagging.deleteTagsOfArticle(articleId, tagIdsForDelete)
    ArticleTagging.insertTagsOfArticle(articleId, tagIdsForInsert)
  }

}

