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

  get("/:id/:commitId/"){ implicit dbSession =>
    val articleId = params.getOrElse("id", redirectFatal("/")).toLong
    viewFunc(articleId, params.get("commitId"))
  }
  val view = get("/:id"){ implicit dbSession =>
    val articleId = params.getOrElse("id", redirectFatal("/")).toLong
    viewFunc(articleId)
  }

  private def viewFunc(articleId: Long, commitIdOpt: Option[String] = None)
    (implicit dbSession: DBSession): Any = {
    val userId    = turqey.servlet.SessionHolder.user.get.id

    val articleRec = Article.find(articleId).filter( _.published ).getOrElse(redirectFatal("/"))
    val latestEdit = ArticleHistory.findLatestsByIds(Seq(articleId)).get(articleId)

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
    
    def article = commitIdOpt
      .map(       RepositoryUtil.articleAt(articleId, _) )
      .getOrElse( RepositoryUtil.headArticle(articleId, "master") )
    val tags = {
      val allTags = Tag.findAll().map( x => (x.id, x) ).toMap
      article.tagIds.map( allTags(_) )
    }
    
    jade("/article/view", 
      "article"     -> articleRec,
      "title"       -> article.title,
      "content"     -> Markdown.html(article.content),
      "latestEdit"  -> latestEdit,
      "tags"        -> tags,
      "attachments" -> article.attachments,
      "comments"    -> comments,
      "stockers"    -> stockers,
      "stocked"     -> stocked,
      "count"       -> count)
  }

  val edit = get("/:id/edit"){ implicit dbSession =>
    val articleId = params.getOrElse("id", redirectFatal("/")).toLong
    val articleRec = Article.find(articleId).getOrElse(redirectFatal("/"))
    if (!articleRec.editable) { redirectFatal("/") }
    
    def branch: String = articleRec.draft()
      .map( x => "draft" )
      .getOrElse( "master" )
    
    def article = RepositoryUtil.headArticle(articleId, branch)
    val tags = {
      val allTags = Tag.findAll().map( x => (x.id, x) ).toMap
      article.tagIds.map( allTags(_) )
    }

    jade("/article/edit", 
      "article"     -> Some(articleRec),
      "title"       -> article.title,
      "content"     -> article.content,
      "tags"        -> tags,
      "attachments" -> article.attachments)
  }

  val history = get("/:id/history"){ implicit dbSession =>
    val articleId = params.getOrElse("id", redirectFatal("/")).toLong
    val articleRec = Article.find(articleId).getOrElse(redirectFatal("/"))
    val histories = ArticleHistory.findAllBy(sqls.eq(ArticleHistory.ah.articleId, articleId))

    jade("article/history", 
      "article"   -> articleRec,
      "histories" -> histories)
  }

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
        
        val articleRec = Article.find(articleId)
        
        val commenterIds = ArticleComment.getCommenterIds(articleId).toSet + articleRec.get.ownerId - userId
        
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

  post("/:id"){ implicit dbSession =>
    val idOpt     = params.get("id")
    val title     = params.getOrElse("title", "").toString
    val content   = params.getOrElse("content", "").toString
    val tagIds    = multiParams("tagIds")
    val tagNames  = multiParams("tagNames")
    val attachments = {
      multiParams("attIds")    zip
      multiParams("attNames")  zip
      multiParams("attMimes")  zip
      multiParams("attIsImgs") map {
        case (((id, name), mime), isImage) => Attachment(
          id      = id.toLong,
          name    = name,
          mime    = mime,
          isImage = isImage.toBoolean
        )
      }
    }

    save(
      idOpt,
      title,
      content,
      tagIds,
      tagNames,
      attachments
    )
  }
  post("/"){ implicit dbSession =>
    val idOpt     = params.get("id")
    val title     = params.getOrElse("title", "").toString
    val content   = params.getOrElse("content", "").toString
    val tagIds    = multiParams("tagIds")
    val tagNames  = multiParams("tagNames")
    val attachments = {
      multiParams("attIds")    zip
      multiParams("attNames")  zip
      multiParams("attMimes")  zip
      multiParams("attIsImgs") map {
        case (((id, name), mime), isImage) => Attachment(
          id      = id.toLong,
          name    = name,
          mime    = mime,
          isImage = isImage.toBoolean
        )
      }
    }

    save(
      idOpt,
      title,
      content,
      tagIds,
      tagNames,
      attachments
    )
  }

  def save(
      idOpt: Option[String],
      title: String,
      content: String,
      tagIds: Seq[String],
      tagNames: Seq[String],
      attachments: Seq[Attachment])
    (implicit dbSession: DBSession): Any = {
    val user = turqey.servlet.SessionHolder.user.get
    
    val articleRec = idOpt.filter( !_.isEmpty )
      .map( x => 
        Article.find(x.toLong).map(
          _.copy(
            title     = title,
            content   = content,
            published = true
          ).save()
        ).getOrElse( redirectFatal("/") )
      ).getOrElse(
        Article.create(
          title     = title,
          content   = content,
          ownerId   = user.id,
          published = true
        )
      )
    if (!articleRec.editable) { redirectFatal("/") }
    
    val articleId = articleRec.id

    articleRec.draft().foreach(_.destroy())

    Article.getStockers(articleId).foreach { s =>
      ArticleNotification.create(
        articleId  = articleId,
        notifyToId = s.id,
        notifyType = ArticleNotification.TYPES.UPDATE
      )
    }

    val newTagIds = refreshTaggings(articleId, tagIds, tagNames)
    
    val headCommit = RepositoryUtil.saveAsMaster(
      articleId,
      title,
      content,
      newTagIds,
      Ident(user),
      attachments
    )

    ArticleHistory.create(articleId, headCommit.getObjectId.name, Some(user.id))

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
      "title"      -> "",
      "content"    -> "",
      "tags"       -> Seq())
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
  
  get("/drafts/"){ implicit dbSession =>
    val user = turqey.servlet.SessionHolder.user.get
    val drafts = Draft.findAllBy(
      sqls.eq(Draft.column.ownerId, user.id)
    )
    
    jade("/article/drafts", 
      "drafts"   -> drafts)
  }
  
  post("/draft/"){ implicit dbSession =>
    contentType = "text/json"
    val user = turqey.servlet.SessionHolder.user.get
    
    val content   = params.getOrElse("content", "").toString
    val title     = params.getOrElse("title", "").toString
    val articleId = params.get("id").map(_.toLong).getOrElse(
      Article.create(
        title   = title,
        content = content,
        ownerId = user.id
      ).id
    )
    val attachments = {
      multiParams("attIds")    zip
      multiParams("attNames")  zip
      multiParams("attMimes")  zip
      multiParams("attIsImgs") map {
        case (((id, name), mime), isImage) => Attachment(
          id      = id.toLong,
          name    = name,
          mime    = mime,
          isImage = isImage.toBoolean
        )
      }
    }

    Draft.findBy(sqls.eq(Draft.column.articleId, articleId))
      .map(
        _.copy(
          title   = title,
          content = content
        ).save()
      )
      .getOrElse(
        Draft.create(
          articleId = articleId,
          title     = title,
          content   = content,
          ownerId   = user.id
        )
      )
    
    val newTagIds = registerTags(multiParams("tagIds"), multiParams("tagNames"))
    
    RepositoryUtil.saveAsDraft(
      articleId,
      title,
      content,
      newTagIds,
      Ident(user),
      attachments
    )
    
    Json.toJson(Map("articleId" -> articleId))
  }

  private def refreshTaggings(articleId: Long, tagIds: Seq[String], tagNames: Seq[String])
    (implicit dbSession: DBSession): Seq[Long] = {
    val tagIdsOld = ArticleTagging.findAllBy(sqls.eq(ArticleTagging.at.articleId, articleId)).map( x => x.tagId )
    val tagIdsNew = registerTags(tagIds, tagNames)

    val tagIdsForDelete = tagIdsOld diff tagIdsNew distinct
    val tagIdsForInsert = tagIdsNew diff tagIdsOld distinct

    ArticleTagging.deleteTagsOfArticle(articleId, tagIdsForDelete)
    ArticleTagging.insertTagsOfArticle(articleId, tagIdsForInsert)
    tagIdsNew
  }
  
  private def registerTags(tagIds: Seq[String], tagNames: Seq[String])
    (implicit dbSession: DBSession): Seq[Long] = {
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

    tagIdName.map { case (id, name) => id }
  }

}

