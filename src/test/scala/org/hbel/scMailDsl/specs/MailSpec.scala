package org.hbel.scMailDsl.specs

import java.io.File

import org.scalatest._

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await

class MailSpec extends FlatSpec with Matchers {
  import org.hbel.scMailDsl.Email._
  import org.hbel.scMailDsl.MailServer

  "'compose a mail'" should "create a new Mail instance" in {
    val m = compose a mail
    m should not be null
  }

  it should "allow to set a sender" in {
    val m = compose a mail from "test" @: "foo.de"
    m.from shouldEqual ("test" @: "foo.de")
  }

  it should "allow to set one or more recipients" in {
    val m = compose a mail to "test" @: "foo.de"
    m.to shouldEqual ("test" @: "foo.de")
    val n = compose a mail to "test" @: "foo.de" and "foo" @: "bar.com"
    n.to shouldEqual "test@foo.de,foo@bar.com"
  }

  it should "allow to set a subject" in {
    val m = compose a mail regarding "A sample"
    m.subject shouldEqual "A sample"
  }

  it should "allow to set a message text" in {
    val m = compose a mail containing "A text"
    m.message shouldEqual "A text"
  }

  "Sending a mail" should "fail if not all mail properties are set" in {
    val m = compose a mail containing "A text" from "me@you" regarding "Something"
    implicit val server = MailServer("foo", 1)
    m.send.isLeft should be(true)
  }

  "Creating a mail" should "not fail if all properties are set" in {
    val m = compose a mail containing "A text" from "me@you.com" to "foo@bar.com" regarding "Something"
    m.validate should be (true)
  }

  it should "not fail for a html message" in {
    val m = compose a mail containing <html><body>Hello World</body></html> from
      "me@you.com" to "foo@bar.com" regarding "Something"
    m.validate should be (true)
  }

  it should "not fail for attachments" in {
    val m = compose a mail containing "Hello World" from
      "me@you.com" to "foo@bar.com" regarding "Something" attach "foo.bar" attach "bar.foo"
    m.validate should be (true)
  }

  /*it should "send an email" in {
    implicit val server = MailServer("foo", 1)
    val m = compose a mail from "noreply" @: "foo.de" to "bar@foo.de" regarding "Whatever" containing "Foobar"
    m.send match {
      case Left(s) => throw new Exception(s"Missing values: ${s}")
      case Right(f) => {
        f.onComplete( _ match {
          case Failure(e) => throw new Exception(s"Sending mail failed: ${e}")
          case Success(s) => s"Sending mail was a success: ${s}"
        })
        Await.ready(f, 5 seconds)
      }
    }
  }*/
}

