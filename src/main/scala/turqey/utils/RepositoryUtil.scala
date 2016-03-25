package turqey.utils

import gristle._
import gristle.GitRepository._
import java.io.File

object RepositoryUtil  {
  
  def articleDir(id: Long): GitRepository = {
    new GitRepository(
      new File(FileUtil.articleBaseDir, id.toString + ".git"),
      new Ident("", "")
    )
  }
  
}