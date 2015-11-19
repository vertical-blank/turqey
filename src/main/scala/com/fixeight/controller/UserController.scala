package com.fixeight.controller

import org.scalatra._
import javax.servlet.http.HttpServletRequest
import io.github.gitbucket.markedj._
import scalikejdbc._

import com.fixeight.entity._
import com.fixeight.utils._
import com.fixeight.article._

import scalaz._
import scalaz.Scalaz._

class UserController extends ControllerBase  {
  override val path = "/user"

  val view = get("/:id"){
  }

  val edit = get("/:id/edit"){
  }

}

