package org.hbel.scMailDsl

import org.apache.commons.mail.SimpleEmail

object Email {

  /**
    * Operator to combine names and domains into fully-fledged mail-addresses
    *
    * @param s string
    */
  implicit class MailString(val s: String) extends AnyRef {
    require(!s.contains('@'))

    /**
      *
      * @param t string
      * @return an email address
      */
    def @:(t: String): String = {
      require(!t.contains('@'))
      t + "@" + s
    }
  }

  /**
    * DSL convience function. Creates a new empty Mail instance
    *
    * @return
    */
  def mail = new Mail("", "", "", "", new SimpleEmail, Nil)

  /**
    * DSL convenience object. Allows natural language construct "compose a mail" in source code.
    */
  object compose {
    def a(m: Mail): Mail = m
  }

}
