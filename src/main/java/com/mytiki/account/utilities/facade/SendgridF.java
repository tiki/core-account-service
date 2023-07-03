/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class SendgridF {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String FROM_ADDRESS = "no-reply@mytiki.com";
    private static final String FROM_NAME = "TIKI";
    private static final String CONTENT_TYPE_TEXT = "text/plain";
    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final String ENDPOINT_MAIL_SEND = "mail/send";
    private final SendGrid sendGrid;

    public SendgridF(String apiKey) {
        this.sendGrid = new SendGrid(apiKey);
    }

    public boolean send(String to, String subject, String htmlContent, String textContent) {
        Mail mail = new Mail();
        mail.setFrom(new Email(FROM_ADDRESS, FROM_NAME));
        mail.setSubject(subject);

        if (textContent != null && !textContent.isBlank())
            mail.addContent(new Content(CONTENT_TYPE_TEXT, textContent));
        if (htmlContent != null && !htmlContent.isBlank())
            mail.addContent(new Content(CONTENT_TYPE_HTML, htmlContent));

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(to));
        mail.addPersonalization(personalization);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint(ENDPOINT_MAIL_SEND);
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            if (response.getStatusCode() < 200 || response.getStatusCode() > 299) {
                logger.debug("Error with " + ENDPOINT_MAIL_SEND + ": {}", response.getBody());
                return false;
            }
        } catch (IOException ex) {
            logger.error("Error with " + ENDPOINT_MAIL_SEND, ex);
            return false;
        }
        return true;
    }
}
