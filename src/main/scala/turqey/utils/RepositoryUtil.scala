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
  
}

object Ident {
  def apply(user: turqey.servlet.UserSession) = new gristle.GitRepository.Ident(user.name, user.email)
}
