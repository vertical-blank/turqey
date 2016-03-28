package turqey.utils

import gristle._
import gristle.GitRepository._
import java.io.File

object RepositoryUtil {
  
  def getArticleRepo(id: Long): GitRepository = {
    GitRepository.getInstance(
      new File(FileUtil.articleBaseDir, id.toString + ".git")
    )
  }
  
  private def commitArticleToBranch(branchName: String, article: Article, ident: Ident): Unit = {
    getArticleRepo(article.id).branch(branchName).commit(
      article.constructDir(), 
      "%tY/%<tm/%<td %<tH:%<tM:%<tS" format new java.util.Date(),
      ident
    )
  }
  
  def saveAsDraft(id: Long, title: String, content: String, tagIds: Seq[Long], ident: Ident) = {
    val repo = getArticleRepo(id)
    if (!repo.branch("master").exists()) {
      repo.initialize("initial commit", ident)
    }
    val draft = repo.branch("draft")
    if (!draft.exists()) {
      repo.branch("master").createNewBranch("draft")
    }
    
    commitArticleToBranch("draft", Article(id, title, content, tagIds), ident)
  }
  
  def saveAsMaster(id: Long, title: String, content: String, tagIds: Seq[Long], ident: Ident) = {
    val repo = getArticleRepo(id)
    if (!repo.branch("master").exists()) {
      repo.initialize("initial commit", ident)
    }
    commitArticleToBranch("master", Article(id, title, content, tagIds), ident)
  }
  
  case class Article(id: Long, title: String, content: String, tagIds: Seq[Long]) {  
    val ARTICLE_MD     = "Article.md"
    val ARTICLE_CONFIG = "articleConf.json"
    
    def configMap = Map("title" -> this.title, "tagIds" -> this.tagIds)
    
    def constructDir() = new gristle.GitRepository.Dir()
      .put(ARTICLE_MD,     this.content.getBytes())
      .put(ARTICLE_CONFIG, Json.toJson(this.configMap).getBytes())
  }
  
}

object Ident {
  def apply(user: turqey.servlet.UserSession) = new gristle.GitRepository.Ident(user.name, user.email)
}
