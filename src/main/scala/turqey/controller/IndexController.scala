package turqey.controller

import org.scalatra._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet._
import turqey.html

class IndexController extends ControllerBase {
  override val key = ""
  override val shouldLoggedIn = false

  val entry = get("/") {
  
    val articles = 
      if (SessionHolder.user isDefined){
        Article.findAll()
      }
      else {
        redirect(url(login))
      }

    val stocks = ArticleStock.findAllBy(sqls.eq(ArticleStock.column.userId, SessionHolder.user.get.id)).map( x => x.articleId )

    html.index(articles, stocks)
  }
  
  val login = get("/login") {
    html.login()
  }

  post("/login") {
    val id   = params.get("email")
    val pass = params.get("password")

    val digestedPass = turqey.utils.Digest.get(pass.get)

    User.findBy(sqls.eq(User.u.email, id).and.eq(User.u.password, digestedPass))
      match {
        case Some(user: User) => {
          session("user") = new UserSession(user.id, user.name, user.imgUrl, user.root)
          
          redirect(url(entry))
        }
        case None => html.login()
      }
    
  }

}

