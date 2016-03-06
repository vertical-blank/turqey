package turqey.controller

import org.scalatra._
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._

import turqey.utils.Implicits._

class TagController extends AuthedController with ScalateSupport {
  override val path = "tag"

  val list = get("/"){ implicit dbSession =>
    val tags = Tag.findAllWithArticleCount()

    jade("/tag/list", "tags" -> tags)
  }

  val view = get("/:id"){ implicit dbSession =>
    val tagId = params.getOrElse("id", redirectFatal("/")).toLong
    
    val tag = Tag.find(tagId).getOrElse(redirectFatal("/"))
    val articles = Article.findTagged(tagId)

    val userId    = turqey.servlet.SessionHolder.user.get.id

    val followed = {
      val tf = TagFollowing.tf
      TagFollowing.findBy(sqls
        .eq(tf.userId, userId).and
        .eq(tf.followedId, tagId)
      ).isDefined
    }
    
    val followers = Tag.getFollowers(tagId)

    jade("/tag/view", 
      "tag" -> tag, 
      "articles" -> articles, 
      "followers" -> followers, 
      "followed" -> followed)
  }

  get("/followings"){ implicit dbSession =>
    val userId    = turqey.servlet.SessionHolder.user.get.id
    val ids = TagFollowing.findAllBy(sqls.eq(TagFollowing.tf.userId, userId)).map(_.followedId)
    val tags = Tag.findAllWithArticleCount(ids)

    jade("/tag/list", "tags" -> tags)
  }
  
  post("/:id/follow"){ implicit dbSession =>
    contentType = "text/json"
    
    val tagId  = params.getOrElse("id", redirectFatal("/")).toLong
    val userId = turqey.servlet.SessionHolder.user.get.id

    val ret = {
      val tf = TagFollowing.tf 
      TagFollowing.findBy(sqls
        .eq(tf.userId, userId).and
        .eq(tf.followedId, tagId)
      ) match {
        case Some(a)  => {
          a.destroy()
          "unfollow"
        }
        case None     => {
          TagFollowing.create(userId = userId, followedId = tagId)
          "follow"
        }
      }
    }
    
    Json.toJson(Map("status" -> ret))
  }

}
