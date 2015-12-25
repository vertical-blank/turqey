
package turqey.util
import io.github.gitbucket.markedj._
import io.github.gitbucket.markedj.Utils._

import java.text.Normalizer
import java.util.regex.Pattern
import java.util.Locale

object Markdown {

  def html(markdown: String): String = {
    val escaped = escapeTaskList(markdown)
    
    val opts = new Options()
    opts.setBreaks(true)
    
    Marked.marked(escaped, opts, new TurqyRenderer(opts))
  }

  def escapeTaskList(text: String): String = {
    Pattern.compile("""^( *)- \[([x| ])\] """, Pattern.MULTILINE).matcher(text).replaceAll("$1* task:$2: ")
  }

  class TurqyRenderer(options: Options) extends Renderer(options) {
    
    override def table(header: String, body: String):String = {
        "<table class=\"bordered striped highlight\">\n<thead>\n" + header + "</thead>\n<tbody>\n" + body + "</tbody>\n</table>\n"
    }

    override def text(text: String): String = {
      // convert task list to checkbox.
      convertCheckBox(text, false)
    }

    def convertCheckBox(text: String, hasWritePermission: Boolean): String = {
      // TODO must be srround text with label tag.
      val disabled = if (hasWritePermission) "" else "disabled"
      text.replaceAll("task:x:", """<input type="checkbox" class="filled-in" checked="checked" """ + disabled + "/>")
        .replaceAll("task: :", """<input type="checkbox" class="filled-in" """ + disabled + "/>")
    }

  }

}

