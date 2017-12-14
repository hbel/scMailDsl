package org.hbel.scMailDsl

/**
  * Representation of a mail server
  *
  * @param hostName     host name or address
  * @param smtpPort     port to use
  * @param sslOnConnect should ssl be used
  * @param tlsRequired  should tls be enforced
  */
case class MailServer(hostName: String, smtpPort: Int,
                      sslOnConnect: Boolean = false, tlsRequired: Boolean = false) {
  require(!hostName.isEmpty, "hostName may not be empty")
  require(smtpPort > 0, "smtpPort must be a positive number")
}
