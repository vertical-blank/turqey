package turqey.utils

import gristle._
import gristle.GitRepository._
import java.io.File


object RepositoryUtil {
  import scala.language.implicitConversions
  import LockByVal.withLock
  
  implicit class RichBranch(b: GitRepository#Branch) {
    def existsOr( f: => GitRepository#Branch ): GitRepository#Branch = if(b.exists) b else f
  }
  
  def getArticleRepo(id: Long): GitRepository = {
    GitRepository.getInstance(
      new File(FileUtil.articleBaseDir, id.toString + ".git")
    )
  }
  
  private def commitArticleToBranch(branch: GitRepository#Branch, article: ArticleWhole, ident: Ident): GitRepository#Commit = {
    branch.commit(
      article.constructDir(), 
      "%tY/%<tm/%<td %<tH:%<tM:%<tS" format new java.util.Date(),
      ident
    )
  }
  
  def saveAsDraft(id: Long, title: String, content: String, tagIds: Seq[Long], ident: Ident)
    (implicit repo: GitRepository = getArticleRepo(id)): Unit = {
    withLock (repo.getDirectory.toString) {
      val master = repo.branch("master")
        .existsOr( repo.initialize("initial commit", ident).branch("master") )
      val draft  = repo.branch("draft").existsOr( master.createNewBranch("draft") )
      
      commitArticleToBranch(draft, ArticleWhole(id, title, content, tagIds), ident)
    }
  }
  
  def saveAsMaster(id: Long, title: String, content: String, tagIds: Seq[Long], ident: Ident)
    (implicit repo: GitRepository = getArticleRepo(id)): Unit = {
    withLock (repo.getDirectory.toString) {
      val master = repo.branch("master")
        .existsOr( repo.initialize("initial commit", ident).branch("master") )
      val draft  = repo.branch("draft").existsOr( master.createNewBranch("draft") )

      commitArticleToBranch(draft, ArticleWhole(id, title, content, tagIds), ident)

      draft.mergeTo(master, ident)

      master.head.addTag(
        "%tY/%<tm/%<td %<tH:%<tM:%<tS" format new java.util.Date(),
        "%tY/%<tm/%<td %<tH:%<tM:%<tS" format new java.util.Date(),
        ident)
    }
  }
  
  def headArticle(id: Long, branchName: String)
    (implicit repo: GitRepository = getArticleRepo(id)): ArticleWhole = {
    articleAt(id, repo.branch(branchName).head)
  }
  def articleAt(id: Long, commitId: String)
    (implicit repo: GitRepository = getArticleRepo(id)): ArticleWhole = {
    articleAt(id, new repo.Commit(commitId))
  }

  def articleAt(id: Long, commit: GitRepository#Commit): ArticleWhole = {
    val dir = commit.getDir
    val content = new String(dir.file(ArticleWhole.ARTICLE_MD).bytes)
    val attrs   = Json.parseAs[ArticleAttrs](new String(dir.file(ArticleWhole.ARTICLE_ATTRS).bytes))
    
    ArticleWhole(id, attrs.title, content, attrs.tagIds)
  }

  def listTags(id: Long)(implicit repo: GitRepository = getArticleRepo(id)): Seq[GitRepository#Tag] = {
    import collection.JavaConversions._
    repo.listTags()
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
