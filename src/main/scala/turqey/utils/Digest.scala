
package turqey.utils

import java.security.MessageDigest

object Digest {
  
  def get(message: String):String = {
    val md = MessageDigest.getInstance("SHA-256")

    md.reset()
    md.update(message.getBytes)

    md.digest().map( x => "%02x".format(x & 0xff) ).mkString("")
  }
  
}

