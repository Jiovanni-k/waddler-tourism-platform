package org.example.rest.notification;

public class EmailMessageBuilder {

    private String recipient;
    private String subject;
    private String messageBody;

    private EmailMessageBuilder() {}

    public static EmailMessageBuilder builder() {
        return new EmailMessageBuilder();
    }

    public EmailMessageBuilder to(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public EmailMessageBuilder subject(String subject) {
        this.subject = subject;
        return this;
    }

    public EmailMessageBuilder body(String messageBody) {
        this.messageBody = messageBody;
        return this;
    }

    public EmailDetails build() {
        if (recipient == null || recipient.isBlank())
            throw new IllegalStateException("Email recipient is required");
        if (subject == null || subject.isBlank())
            throw new IllegalStateException("Email subject is required");
        if (messageBody == null || messageBody.isBlank())
            throw new IllegalStateException("Email body is required");

        return new EmailDetails(recipient, subject, messageBody);
    }
}