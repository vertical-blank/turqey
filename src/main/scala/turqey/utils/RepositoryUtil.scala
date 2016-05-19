package turqey.utils

import glitch._
import glitch.GitRepository._
import java.io.File

class WritableArticleRepository(id: Long, ident: Ident) extends ArticleRepository(id, Some(ident))
class ReadOnlyArticleRepository(id: Long) extends ArticleRepository(id, None)

abstract sealed class ArticleRepository(id: Long, identOpt: Option[Ident] = None) {
  import scala.language.implicitConversions

  lazy val ident = identOpt.get

  val repo: GitRepository = GitRepository.getInstance(
    new File(FileUtil.articleBaseDir, id.toString + ".git"))

  def withLock[T](f: => T) = LockByVal.withLock(repo.getDirectory.toString)(f)
  def existsOr(b: GitRepository#Branch)( f: => GitRepository#Branch ): GitRepository#Branch = if(b.exists) b else f

  val master = withLock {
    new MasterBranch(existsOr(repo.branch("master"))( repo.initialize("initial commit", ident).branch("master") ))
  }
  
  def draftName = "draftOf" + ident.userId.toString
  lazy val draft = new DraftBranch(
    existsOr(repo.branch(draftName))( master.createNewBranch(draftName)),
    master)
  
  abstract class Branch(branch: GitRepository#Branch) extends repo.Branch(branch.name) {
    def articleAt(commitId: String): ArticleWhole = articleAt(new repo.Commit(commitId))
    def articleAt(commit: GitRepository#Commit): ArticleWhole = {
      val dir = commit.getDir
      val content = new String(dir.file(ArticleWhole.ARTICLE_MD).bytes)
      val attrs   = Json.parseAs[ArticleAttrs](new String(dir.file(ArticleWhole.ARTICLE_ATTRS).bytes))
  
      ArticleWhole(id, attrs.title, content, attrs.tagIds, attrs.attachments)
    }
    def headArticle: ArticleWhole = articleAt(head)

    def commit(article: ArticleWhole): GitRepository#Commit = branch.commit(
      article.constructDir(), 
      "%tY/%<tm/%<td %<tH:%<tM:%<tS" format new java.util.Date(),
      ident
    )

    def save(title: String, content: String, tagIds: Seq[Long], attachments: Seq[Attachment]): GitRepository#Commit
  }

  class MasterBranch(branch: GitRepository#Branch) extends Branch(branch) {
    override def save(title: String, content: String, tagIds: Seq[Long], attachments: Seq[Attachment]) = withLock {
      commit(ArticleWhole(id, title, content, tagIds, attachments))

      master.mergeTo(draft, ident)
      draft.mergeTo(master, ident, true)

      val head = master.head
      head.addTag(
        "%tY%<tm%<td_%<tH%<tM%<tS" format new java.util.Date(),
        "%tY%<tm%<td_%<tH%<tM%<tS" format new java.util.Date(),
        ident)
      head
    }
  }

  class DraftBranch(branch: GitRepository#Branch, master: MasterBranch) extends Branch(branch) {
    def isBehindMaster: Boolean = this.isBehind(master)
    override def save(title: String, content: String, tagIds: Seq[Long], attachments: Seq[Attachment]) = withLock {
      commit(ArticleWhole(id, title, content, tagIds, attachments))
      draft.head
    }
    def isMergableFromMaster: Boolean = master.isMergableTo(this)
    def isMergableToMaster: Boolean = this.isMergableTo(master)

    def mergeFromMaster: Unit = master.mergeTo(this, ident)
    def mergeToMaster: Unit = this.mergeTo(master, ident)
  }

  def listTags(id: Long): Seq[GitRepository#Tag] = {
    import collection.JavaConversions._
    repo.listTags()
  }
}

case class Attachment(id: Long, name: String, isImage: Boolean, mime: String, size: Long)
case class ArticleAttrs(title: String, tagIds: Seq[Long], attachments: Seq[Attachment])

case class ArticleWhole(id: Long, title: String, content: String, tagIds: Seq[Long], attachments: Seq[Attachment]) {
  def attrs = ArticleAttrs(this.title, this.tagIds, this.attachments)
  def constructDir() = new GitRepository.Dir()
    .put(ArticleWhole.ARTICLE_MD,    this.content.getBytes())
    .put(ArticleWhole.ARTICLE_ATTRS, Json.toJson(this.attrs).getBytes())
}
object ArticleWhole {
  val ARTICLE_MD    = "Article.md"
  val ARTICLE_ATTRS = "ArticleAttrs.json"
}

case class Ident(userId: Long, name: String, email: String) extends GitRepository.Ident(name, email)

object Ident {
  def apply(user: turqey.servlet.UserSession): Ident = Ident(user.id, user.name, user.email)
}

class ConflictException extends Exception

