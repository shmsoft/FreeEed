package org.freeeed.main;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.freeeed.mail.EmailUtil;
import org.junit.Test;

public class EmailUtilTest {

    @Test
    public void testMessageIdValidation() {
        assertTrue(EmailUtil.isMessageId("<ABCD@GOOGLE.COM>"));
        assertTrue(EmailUtil.isMessageId("<A@X.COM>"));
        assertTrue(EmailUtil.isMessageId("<12345@X12345>"));
        assertFalse(EmailUtil.isMessageId("TEST@GOOGLE.COM"));
        assertFalse(EmailUtil.isMessageId("<TEST@GOOGLE.COM"));
        assertFalse(EmailUtil.isMessageId("<@GOOGLE.COM>"));
    }

    @Test
    public void testEmailSend() {
        String subject = "test shalom";
        String message = "shalom shalom";
        boolean status = EmailUtil.sendEmail(subject, message);
        assertTrue(status);
    }

}
