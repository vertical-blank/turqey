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
  
  def saveAsDraft(id: Long, title: String, content: String, tagIds: Seq[Long], ident: Ident, attachments: Seq[Attachment])
    (implicit repo: GitRepository = getArticleRepo(id)): GitRepository#Commit = {
    withLock (repo.getDirectory.toString) {
      val master = repo.branch("master")
        .existsOr( repo.initialize("initial commit", ident).branch("master") )
      val draft  = repo.branch("draft").existsOr( master.createNewBranch("draft") )
      
      commitArticleToBranch(draft, ArticleWhole(id, title, content, tagIds, attachments), ident)
    }
  }
  
  def saveAsMaster(id: Long, title: String, content: String, tagIds: Seq[Long], ident: Ident, attachments: Seq[Attachment])
    (implicit repo: GitRepository = getArticleRepo(id)): GitRepository#Commit = {
    withLock (repo.getDirectory.toString) {
      val master = repo.branch("master")
        .existsOr( repo.initialize("initial commit", ident).branch("master") )
      val draft  = repo.branch("draft").existsOr( master.createNewBranch("draft") )

      commitArticleToBranch(draft, ArticleWhole(id, title, content, tagIds, attachments), ident)

      draft.mergeTo(master, ident)

      val head = master.head
      head.addTag(
        "%tY/%<tm/%<td %<tH:%<tM:%<tS" format new java.util.Date(),
        "%tY/%<tm/%<td %<tH:%<tM:%<tS" format new java.util.Date(),
        ident)
      head
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
    
    ArticleWhole(id, attrs.title, content, attrs.tagIds, attrs.attachments)
  }

  def listTags(id: Long)(implicit repo: GitRepository = getArticleRepo(id)): Seq[GitRepository#Tag] = {
    import collection.JavaConversions._
    repo.listTags()
  }

}

case class Attachment(id: Long, name: String, isImage: Boolean, mime: String, size: Long)
case class ArticleAttrs(title: String, tagIds: Seq[Long], attachments: Seq[Attachment])

case class ArticleWhole(id: Long, title: String, content: String, tagIds: Seq[Long], attachments: Seq[Attachment]) {
  def attrs = ArticleAttrs(this.title, this.tagIds, this.attachments)
  def constructDir() = new gristle.GitRepository.Dir()
    .put(ArticleWhole.ARTICLE_MD,    this.content.getBytes())
    .put(ArticleWhole.ARTICLE_ATTRS, Json.toJson(this.attrs).getBytes())
}
object ArticleWhole {
  val ARTICLE_MD    = "Article.md"
  val ARTICLE_ATTRS = "ArticleAttrs.json"
}

object Ident {
  import gristle.GitRepository.{Ident => JIdent}
  def apply(name: String, email: String): JIdent = new JIdent(name, email)
  def apply(user: turqey.servlet.UserSession): JIdent = apply(user.name, user.email)
}
