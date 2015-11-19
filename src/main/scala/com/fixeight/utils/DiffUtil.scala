package com.fixeight.utils

import difflib._
import java.util.Arrays.asList
import scala.collection.JavaConversions._

object DiffUtil {

  def uniDiff(original: String, revised: String): Seq[String] = {
    def split = (s:String) => s.replaceAll("\r\n", "\n").replaceAll("\r", "\n").split("\n").toList 
    val patch = DiffUtils.diff(
      split(original),
      split(revised)
    )

    DiffUtils.generateUnifiedDiff("original", "revised", asList(original), patch, -1)
  }
  
  def restore(current: String, diffs: Seq[String]): String = {
    
    
    "a"
  }

}



