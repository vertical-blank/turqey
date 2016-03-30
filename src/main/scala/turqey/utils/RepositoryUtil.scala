package turqey.utils

import gristle._
import gristle.GitRepository._
import java.io.File

object RepositoryUtil {
  import scala.language.implicitConversions
  
  implicit class RichBranch(b: GitRepository#Branch) {
    def orCreate( f: => GitRepository#Branch ): GitRepository#Branch = if(b.exists) b else f
  }
  
  implicit def nameToBranch(s: String)
    (implicit repo: GitRepository): GitRepository#Branch = repo.branch(s)
  
  def getArticleRepo(id: Long): GitRepository = {
    GitRepository.getInstance(
      new File(FileUtil.articleBaseDir, id.toString + ".git")
    )
  }
  
  private def commitArticleToBranch(branch: GitRepository#Branch, article: ArticleWhole, ident: Ident)
    (implicit repo: GitRepository): Unit = {
    branch.commit(
      article.constructDir(), 
      "%tY/%<tm/%<td %<tH:%<tM:%<tS" format new java.util.Date(),
      ident
    )
  }
  
  def saveAsDraft(id: Long, title: String, content: String, tagIds: Seq[Long], ident: Ident)
    (implicit repo: GitRepository = getArticleRepo(id)): Unit = {
    
    val master = repo.branch("master")
      .orCreate( repo.initialize("initial commit", ident).branch("master") )
    val draft  = repo.branch("draft").orCreate( master.createNewBranch("draft") )
    
    commitArticleToBranch(draft, ArticleWhole(id, title, content, tagIds), ident)
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
  
  def attrs = ArticleAttrs(this.title, this.tagIds)
  
  def constructDir() = new gristle.GitRepository.Dir()
    .put(ArticleWhole.ARTICLE_MD,    this.content.getBytes())
    .put(ArticleWhole.ARTICLE_ATTRS, Json.toJson(this.attrs).getBytes())
}
object ArticleWhole {
  val ARTICLE_MD    = "Article.md"
  val ARTICLE_ATTRS = "ArticleAttrs.json"
}

object Ident {
  def apply(user: turqey.servlet.UserSession) = new gristle.GitRepository.Ident(user.name, user.email)
}