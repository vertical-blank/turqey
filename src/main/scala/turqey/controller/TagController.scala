package turqey.controller

import org.scalatra._
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.tag._

import turqey.utils.Implicits._

class TagController extends ControllerBase {
  override val path = "tag"

  val list = get("/"){
    val tags = Tag.findAllWithArticleCount()

    jade("/tag/list", "tags" -> tags)
  }

  val view = get("/:id"){
    val tagId = params.getOrElse("id", redirect("/")).toLong
    
    val tag = Tag.find(tagId).getOrElse(redirect("/"))
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

  get("/followings"){
    val userId    = turqey.servlet.SessionHolder.user.get.id
    val ids = TagFollowing.findAllBy(sqls.eq(TagFollowing.tf.userId, userId)).map(_.followedId)
    val tags = Tag.findAllWithArticleCount(ids)

    jade("/tag/list", "tags" -> tags)
  }
  
  post("/:id/follow"){
    contentType = "text/json"
    
    val tagId  = params.getOrElse("id", redirect("/")).toLong
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
