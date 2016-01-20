package turqey.controller

import com.google.api.client.googleapis.auth.oauth2._
import com.google.api.client.googleapis.javanet._
import com.google.api.client.json.jackson2._
import com.google.api.client.http._
import com.google.api.services.plus._
import com.google.api.services.plusDomains._
import com.google.api.services.plusDomains.model._
import collection.JavaConversions._
import java.util.Arrays.asList
import java.util.Date

import org.scalatra._
import scalikejdbc._

import turqey.entity._
import turqey.servlet._
import turqey.html
import turqey.utils.Implicits._

class GoogleAuthController extends ControllerBase {
  override val path = "googleAuth"
  override val shouldLoggedIn = false
  
  // TODO need to be refactored.
  val googleAuth = get("/") {
    //TODO LoadFromFileOrDB.
    val clientSecret = "9DcPVivtQlBS3YdX-pkxlReG"
    val cliendId = "553877725807-nh4mfmq8di4e2h2tqtbkgu6g36iu2h8r.apps.googleusercontent.com"
    // TODO request.getRequestURL
    val redirectUrl = Some(new java.net.URL(request.getRequestURL.toString))
      .map{ x:java.net.URL => "%s://%s:%s/googleAuth".format(x.getProtocol, x.getHost, x.getPort) }
      .get
    
    val flow = new GoogleAuthorizationCodeFlow.Builder(
      GoogleNetHttpTransport.newTrustedTransport,
      JacksonFactory.getDefaultInstance,
      cliendId,
      clientSecret,
      asList(PlusDomainsScopes.PLUS_PROFILES_READ, PlusDomainsScopes.USERINFO_EMAIL)
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

      googleUser
      .filter{ gu => gu.getDomain == "casleyconsulting.co.jp" }
      .foreach { gu => 
        val email = gu.getEmails.head.getValue
        val user = User.findBy(sqls.eq(User.u.email, email)) match {
          case Some(u) => {
            u.copy(
              name      = gu.getDisplayName,
              imgUrl    = gu.getImage.getUrl,
              lastLogin = Some(new org.joda.time.DateTime())
            ).save()
          }
          case None    => {
            User.create(
              loginId   = "",
              password  = "",
              name      = gu.getDisplayName,
              imgUrl    = gu.getImage.getUrl,
              email     = email,
              lastLogin = Some(new org.joda.time.DateTime()))
          }
        }

        session("user") = new UserSession(user.id, user.name, user.imgUrl, user.root)
      }

      redirect(url(""))
    } else {
      redirect(flow.newAuthorizationUrl.setRedirectUri(redirectUrl).toString)
    }
  }

}

