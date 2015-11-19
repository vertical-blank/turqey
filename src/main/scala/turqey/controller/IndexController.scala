package turqey.controller

import org.scalatra._
import javax.servlet.http.HttpServletRequest
import io.github.gitbucket.markedj._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet._
import turqey.html

import com.google.api.client.googleapis.auth.oauth2._
import com.google.api.client.googleapis.javanet._
import com.google.api.client.json.jackson2._
import com.google.api.client.http._
import com.google.api.services.plus._
import com.google.api.services.plusDomains._
import com.google.api.services.plusDomains.model._
import collection.JavaConversions._
import java.util.Arrays.asList

import scalaz._
import scalaz.Scalaz._

import turqey.utils.Implicits._

class IndexController extends ControllerBase  {
  override val path = "/"

  get("/") {
    val articles = 
    if (SessionHolder.user isDefined){
      implicit val session = AutoSession
      val a = Article.syntax("a")
      withSQL {
        select.from(Article as a)
      }.map(rs => Article(a)(rs)).list.apply
    }
    else {
      redirect(url(login))
    }

    html.index(articles)
  }

  val login = get("/login") {
    html.login()
  }

  // TODO need to be refactored.
  get("/googleAuth") {
    //TODO LoadFromFileOrDB.
    val clientSecret = "9DcPVivtQlBS3YdX-pkxlReG"
    val cliendId = "553877725807-nh4mfmq8di4e2h2tqtbkgu6g36iu2h8r.apps.googleusercontent.com"
    // TODO request.getRequestURL
    val redirectUrl = "http://www.words-words.net:8080/googleAuth"
    
    val flow = new GoogleAuthorizationCodeFlow.Builder(
      GoogleNetHttpTransport.newTrustedTransport,
      JacksonFactory.getDefaultInstance,
      cliendId,
      clientSecret,
      asList(PlusDomainsScopes.PLUS_PROFILES_READ ,PlusDomainsScopes.USERINFO_EMAIL)
    ).build

    val code = params.get("code")
    // if denied  ?error=access_denied 
    if (code.isDefined) {
      val googleResponse = flow.newTokenRequest(code.get).setRedirectUri(redirectUrl).execute
      val payload = googleResponse.parseIdToken().getPayload
      val credential = new GoogleCredential.Builder()
        .setTransport(GoogleNetHttpTransport.newTrustedTransport)
        .setJsonFactory(JacksonFactory.getDefaultInstance)
        .setClientSecrets(cliendId, clientSecret)
        .build
        .setFromTokenResponse(googleResponse)
      val plus = new PlusDomains.Builder(
        GoogleNetHttpTransport.newTrustedTransport,
        JacksonFactory.getDefaultInstance,
        credential).setApplicationName("TurQey").build 

      val googleUser:Option[Person] = try{
          Some(plus.people.get("me").execute)
        } catch {
          case e:Exception => None
        }

      println (googleUser)

      googleUser.foreach { u => 
        session("user") = new UserSession(1, u.getDisplayName(), u.getImage.getUrl)
      } 

      redirect(url("/"))
    } else {
      redirect(flow.newAuthorizationUrl.setRedirectUri(redirectUrl).toString)
    }
  }

}

