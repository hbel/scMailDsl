package org.hbel.scMailDsl

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.mail.{HtmlEmail, MultiPartEmail}

import scala.concurrent.{Future, Promise}
import scala.util.Try

object LastAndType extends Enumeration {
  val None, To, ReplyTo, Cc, Bcc, Attach = Value
}

/**
  * Actual mailing class
  *
  * @param to      Recipient(s)
  * @param cc      Carbon-copy recipient(s)
  * @param bcc     Blind-carbon-copy recipient(s)
  * @param from    Sender
  * @param replyTo Address(es) answers should be send to
  * @param subject Mail subject
  * @param message Mail message
  */
class Mail(val to: String, val cc: String, val bcc: String, val from: String,
           val replyTo: String, val subject: String, val message: String,
           private val last: LastAndType.Value,
           private val emailImpl: org.apache.commons.mail.Email,
           private val attachments: List[java.io.File])
  extends LazyLogging {

  if (validate) {
    emailImpl.setFrom(from)
    emailImpl.setSubject(subject)
    emailImpl match {
      case html: HtmlEmail => html.setHtmlMsg(message)
      case _ => emailImpl.setMsg(message)
    }
    to.split(',').foreach(emailImpl.addTo)
    if (!cc.isEmpty) cc.split(',').foreach(emailImpl.addCc)
    if (!bcc.isEmpty) bcc.split(',').foreach(emailImpl.addBcc)
    if (!replyTo.isEmpty) replyTo.split(',').foreach(emailImpl.addReplyTo)
    emailImpl match {
      case multi: MultiPartEmail => attachments.foreach(multi.attach)
      case _ => ()
    }
  }

  def to(recipient: String): Mail = {
    require(recipient != null && !recipient.isEmpty, "recipient must not be empty")
    require(recipient.contains('@') && recipient.indexOf('@') == recipient.lastIndexOf('@'),
      "recipient must be an email address")
    new Mail(recipient, cc, bcc, from, replyTo, subject, message, LastAndType.To, emailImpl, attachments)
  }

  def cc(recipient: String): Mail = {
    require(recipient != null && !recipient.isEmpty, "recipient must not be empty")
    require(recipient.contains('@') && recipient.indexOf('@') == recipient.lastIndexOf('@'),
      "recipient must be an email address")
    new Mail(to, recipient, bcc, from, replyTo, subject, message, LastAndType.Cc, emailImpl, attachments)
  }

  def bcc(recipient: String): Mail = {
    require(recipient != null && !recipient.isEmpty, "recipient must not be empty")
    require(recipient.contains('@') && recipient.indexOf('@') == recipient.lastIndexOf('@'),
      "recipient must be an email address")
    new Mail(to, cc, recipient, from, replyTo, subject, message, LastAndType.Bcc, emailImpl, attachments)
  }

  def replyTo(recipient: String): Mail = {
    require(recipient != null && !recipient.isEmpty, "recipient must not be empty")
    require(recipient.contains('@') && recipient.indexOf('@') == recipient.lastIndexOf('@'),
      "recipient must be an email address")
    new Mail(to, cc, bcc, from, recipient, subject, message, LastAndType.ReplyTo, emailImpl, attachments)
  }

  def and(data: String): Mail = {
    import LastAndType._
    require(last != None, "can only use 'and' with 'to', 'replyTo', 'bcc', 'cc', or 'attach'")
    require(data != null && !data.isEmpty, "data must not be empty")
    if (last != Attach)
      require(data.contains('@') && data.indexOf('@') == data.lastIndexOf('@'),
        "recipient must be an email address")
    last match {
      case To => new Mail(to + "," + data, cc, bcc, from, replyTo, subject, message, last, emailImpl, attachments)
      case Cc => new Mail(to, cc + "," + data, bcc, from, replyTo, subject, message, last, emailImpl, attachments)
      case Bcc => new Mail(to, cc, bcc + "," + data, from, replyTo, subject, message, last, emailImpl, attachments)
      case ReplyTo => new Mail(to, cc, bcc, from, replyTo + "," + data, subject, message, last, emailImpl, attachments)
      case Attach => attach(data)
      case None => new Mail(to, cc, bcc, from, replyTo, subject, message, last, emailImpl, attachments)
    }
  }

  def from(sender: String): Mail = {
    require(sender != null && !sender.isEmpty, "sender must not be empty")
    require(sender.contains('@') && sender.indexOf('@') == sender.lastIndexOf('@'),
      "sender must be an email address")
    new Mail(to, cc, bcc, sender, replyTo, subject, message, LastAndType.None, emailImpl, attachments)
  }

  def regarding(messageSubject: String): Mail = {
    require(messageSubject != null && !messageSubject.isEmpty, "messageSubject must no be empty")
    new Mail(to, cc, bcc, from, replyTo, messageSubject, message, LastAndType.None, emailImpl, attachments)
  }

  def containing(text: String): Mail = {
    require(text != null && !text.isEmpty, "text must no be empty")
    new Mail(to, cc, bcc, from, replyTo, subject, text, LastAndType.None, emailImpl, attachments)
  }

  def containing(html: scala.xml.Elem): Mail = {
    new Mail(to, cc, bcc, from, replyTo, subject, html.toString(), LastAndType.None, new HtmlEmail, attachments)
  }

  def attach(filename: String): Mail = emailImpl match {
    case html: HtmlEmail => new Mail(to, cc, bcc, from, replyTo, subject, message,
      LastAndType.Attach, html, new File(filename) :: attachments)
    case _ => new Mail(to, cc, bcc, from, replyTo, subject, message,
      LastAndType.Attach, new MultiPartEmail, new File(filename) :: attachments)
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  def send(implicit server: MailServer): Future[String] = {
    require(server != null, "Mail server must be given")

    logger.info(s"Preparing to send message via ${server.hostName}.")
    val sendAction = Promise[String]
    Future {
      if (!validate) {
        val msg = "Email is missing some components"
        logger.error(msg)
        sendAction.failure(new Exception(msg))
      }
      server.auth.foreach(auth => emailImpl.setAuthentication(auth.username, auth.password))
      emailImpl.setHostName(server.hostName)
      emailImpl.setSmtpPort(server.smtpPort)
      emailImpl.setSSLOnConnect(server.sslOnConnect)
      emailImpl.setStartTLSRequired(server.tlsRequired)
      emailImpl.setCharset("utf-8")

      val retVal: Try[String] = Try {
        val rv = emailImpl.send()
        logger.info(s"Message sent.")
        rv
      }

      sendAction.complete(retVal)
    }
    sendAction.future
  }

  def validate: Boolean = !List(to, from, subject, message).contains("")
}

