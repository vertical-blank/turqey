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
  
  private def commitArticleToBranch(branchName: String, article: ArticleWhole, ident: Ident)
    (implicit repo: GitRepository): Unit = {
    repo.branch(branchName).commit(
      article.constructDir(), 
      "%tY/%<tm/%<td %<tH:%<tM:%<tS" format new java.util.Date(),
      ident
    )
  }
  
  def saveAsDraft(id: Long, title: String, content: String, tagIds: Seq[Long], ident: Ident)
    (implicit repo: GitRepository = getArticleRepo(id)): Unit = {
    if (!repo.branch("master").exists()) {
      repo.initialize("initial commit", ident)
    }
    val draft = repo.branch("draft")
    if (!draft.exists()) {
      repo.branch("master").createNewBranch("draft")
    }
    
    commitArticleToBranch("draft", ArticleWhole(id, title, content, tagIds), ident)
  }
  
  def saveAsMaster(id: Long, title: String, content: String, tagIds: Seq[Long], ident: Ident)
    (implicit repo: GitRepository = getArticleRepo(id)): Unit = {
    saveAsDraft(id, title, content, tagIds, ident)
    repo.branch("draft").mergeTo(repo.branch("master"), ident)
  }
  
  def headArticle(id: Long, branchName: String)(implicit repo: GitRepository = getArticleRepo(id)): ArticleWhole = {
    val root = repo.branch(branchName).head().getDir
    
    val content = new String(root.file(ArticleWhole.ARTICLE_MD).bytes)
    val attrs   = Json.parseAs[ArticleAttrs](new String(root.file(ArticleWhole.ARTICLE_ATTRS).bytes))
    
    ArticleWhole(id, attrs.title, content, attrs.tagIds)
  }
}

case class ArticleAttrs(title: String, tagIds: Seq[Long])
case class ArticleWhole(id: Long, title: String, content: String, tagIds: Seq[Long]) {  
  def configMap = Map("title" -> this.title, "tagIds" -> this.tagIds)
  
  def constructDir() = new gristle.GitRepository.Dir()
    .put(ArticleWhole.ARTICLE_MD,    this.content.getBytes())
    .put(ArticleWhole.ARTICLE_ATTRS, Json.toJson(this.configMap).getBytes())
}
object ArticleWhole {
  val ARTICLE_MD    = "Article.md"
  val ARTICLE_ATTRS = "articleAttrs.json"
}

object Ident {
  def apply(user: turqey.servlet.UserSession) = new gristle.GitRepository.Ident(user.name, user.email)
}
