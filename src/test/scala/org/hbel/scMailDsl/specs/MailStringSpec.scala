package org.hbel.scMailDsl.specs

import org.scalatest._

class MailStringSpec extends FlatSpec with Matchers {
  import org.hbel.scMailDsl.Email._

  "@:" should "attach an email address to a domain" in {
    val m = "test" @: "isapag.de"
    m shouldEqual "test@isapag.de"
  }

  it should "not work if one of the strings already contains a @" in {
    an[IllegalArgumentException] should be thrownBy ("foo@bar" @: "bar")
    an[IllegalArgumentException] should be thrownBy ("foo" @: "bar@foo")
  }
}