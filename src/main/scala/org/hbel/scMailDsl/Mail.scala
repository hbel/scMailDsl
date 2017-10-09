package org.hbel.scMailDsl

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.mail.{HtmlEmail, MultiPartEmail}

import scala.concurrent.Future

/**
  * Actual mailing class
  *
  * @param to      Recipient(s)
  * @param from    Sender
  * @param subject Mail subject
  * @param message Mail message
  */
class Mail(val to: String, val from: String, val subject: String, val message: String,
           private val emailImpl: org.apache.commons.mail.Email, private val attachments: List[java.io.File])
  extends LazyLogging {

  if (validate) {
    emailImpl.setFrom(from)
    emailImpl.setSubject(subject)
    emailImpl match {
      case html: HtmlEmail => html.setHtmlMsg(message)
      case _ => emailImpl.setMsg(message)
    }
    to.split(',').foreach(emailImpl.addTo)
    emailImpl match {
      case multi: MultiPartEmail => attachments.foreach(multi.attach)
      case _ => ()
    }
  }

  def to(recipient: String): Mail = {
    require(recipient != null && !recipient.isEmpty, "recipient must not be empty")
    require(recipient.contains('@') && recipient.indexOf('@') == recipient.lastIndexOf('@'),
      "recipient must be an email address")
    new Mail(recipient, from, subject, message, emailImpl, attachments)
  }

  def and(recipient: String): Mail = {
    require(!to.isEmpty, "cannot use 'and' before 'to'")
    require(recipient != null && !recipient.isEmpty, "recipient must not be empty")
    require(recipient.contains('@') && recipient.indexOf('@') == recipient.lastIndexOf('@'),
      "recipient must be an email address")
    new Mail(to + "," + recipient, from, subject, message, emailImpl, attachments)
  }

  def from(sender: String): Mail = {
    require(sender != null && !sender.isEmpty, "sender must not be empty")
    require(sender.contains('@') && sender.indexOf('@') == sender.lastIndexOf('@'),
      "sender must be an email address")
    new Mail(to, sender, subject, message, emailImpl, attachments)
  }

  def regarding(messageSubject: String): Mail = {
    require(messageSubject != null && !messageSubject.isEmpty, "messageSubject must no be empty")
    new Mail(to, from, messageSubject, message, emailImpl, attachments)
  }

  def containing(text: String): Mail = {
    require(text != null && !text.isEmpty, "text must no be empty")
    new Mail(to, from, subject, text, emailImpl, attachments)
  }

  def containing(html: scala.xml.Elem): Mail = {
    new Mail(to, from, subject, html.toString(), new HtmlEmail, attachments)
  }

  def attach(filename: String): Mail = emailImpl match {
    case html: HtmlEmail => new Mail(to, from, subject, message, html, new File(filename) :: attachments)
    case _ => new Mail(to, from, subject, message, new MultiPartEmail, attachments)
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  def send(implicit server: MailServer): Either[String, Future[String]] = {
    require(server != null, "Mail server must be given")

    if (!validate) {
      val msg = "Email is missing some components"
      logger.error(msg)
      Left(msg)
    }
    else {
      logger.info(s"Preparing to send message via ${server.hostName}.")
      val f = Future {
        emailImpl.setHostName(server.hostName)
        emailImpl.setSmtpPort(server.smtpPort)
        emailImpl.setSSLOnConnect(server.sslOnConnect)
        emailImpl.setStartTLSRequired(server.tlsRequired)

        val retVal = emailImpl.send()
        logger.info(s"Message sent.")
        retVal
      }
      Right(f)
    }
  }

  def validate: Boolean = !List(to, from, subject, message).contains("")
}

