package org.hbel.scMailDsl.specs

import org.hbel.scMailDsl.Email._
import org.hbel.scMailDsl.MailServer
import org.scalatest._

class AsyncMailSpec extends AsyncFlatSpec with Matchers {
  "Sending a mail" should "fail if not all mail properties are set" in {
    val m = compose a mail containing "A text" from "me@you" regarding "Something"
    implicit val server: MailServer = MailServer("foo", 1)
    recoverToSucceededIf[Exception](m.send)
  }
}

class MailSpec extends FlatSpec with Matchers {

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

  "Creating a mail" should "not fail if all properties are set" in {
    val m = compose a mail containing "A text" from "me@you.com" to "foo@bar.com" regarding "Something"
    m.validate should be(true)
  }

  it should "not fail for a html message" in {
    val m = compose a mail containing <html>
      <body>Hello World</body>
    </html> from
      "me@you.com" to "foo@bar.com" regarding "Something"
    m.validate should be(true)
  }

  it should "not fail for attachments" in {
    val m = compose a mail containing "Hello World" from
      "me@you.com" to "foo@bar.com" regarding "Something" attach "foo.bar" attach "bar.foo"
    m.validate should be(true)
  }
}

