package org.hbel.scMailDsl

case class Authentication(username: String, password: String) {
  require(!(username == null) && !username.isEmpty)
  require(!(password == null) && !password.isEmpty)
}

/**
  * Representation of a mail server
  *
  * @param hostName     host name or address
  * @param smtpPort     port to use
  * @param sslOnConnect should ssl be used
  * @param tlsRequired  should tls be enforced
  * @param auth         authentication information (if needed)
  */
case class MailServer(hostName: String, smtpPort: Int,
                      sslOnConnect: Boolean = false, tlsRequired: Boolean = false,
                      auth: Option[Authentication] = None) {
  require(!hostName.isEmpty, "hostName may not be empty")
  require(smtpPort > 0, "smtpPort must be a positive number")
}
