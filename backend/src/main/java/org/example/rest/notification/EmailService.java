package org.example.rest.notification;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String emailSender;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    private final String emailHeader = """
            <html>
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <style>
                * { margin:0; padding:0; box-sizing:border-box; }
                body { background:#f5f3ff; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
                .wrapper { background:#f5f3ff; padding:32px 16px; }
                .container { background:#ffffff; max-width:580px; margin:0 auto; border-radius:16px; overflow:hidden; border:1px solid #e8e4f8; }
                .header { background:#534AB7; padding:18px 28px; display:flex; align-items:center; gap:12px; }
                .header img { height:42px; width:42px; border-radius:10px; object-fit:contain; }
                .header-name { font-size:22px; font-weight:600; color:#EEEDFE; letter-spacing:-0.3px; }
                .body { padding:32px 28px; }
                .greeting { font-size:22px; font-weight:600; color:#1a1a2e; margin-bottom:8px; }
                .text { font-size:14px; color:#555; line-height:1.7; margin-bottom:16px; }
                .nights-banner { background:#534AB7; color:#EEEDFE; border-radius:10px; padding:14px 20px; margin:16px 0; text-align:center; font-size:16px; font-weight:600; }
                table.info { width:100%; border-collapse:collapse; margin:16px 0; font-size:13px; }
                table.info tr:nth-child(odd) td { background:#f5f3ff; }
                table.info td { padding:10px 12px; border-bottom:1px solid #ede9fb; color:#1a1a2e; }
                table.info td:first-child { color:#777; width:44%; }
                .badge { display:inline-block; padding:3px 12px; border-radius:20px; font-size:12px; font-weight:500; }
                .badge-pending  { background:#fff8e1; color:#b45309; }
                .badge-success  { background:#ecfdf5; color:#065f46; }
                .badge-danger   { background:#fef2f2; color:#991b1b; }
                .badge-info     { background:#eeedfe; color:#3c3489; }
                .box { border-radius:10px; padding:14px 16px; margin:16px 0; font-size:13px; line-height:1.6; }
                .box-purple  { background:#eeedfe; border-left:3px solid #534AB7; color:#3c3489; }
                .box-warning { background:#fffbeb; border-left:3px solid #f59e0b; color:#78350f; }
                .box-danger  { background:#fef2f2; border-left:3px solid #ef4444; color:#7f1d1d; }
                .box-success { background:#ecfdf5; border-left:3px solid #10b981; color:#064e3b; }
                .big-number { text-align:center; padding:20px; background:#ecfdf5; border-radius:12px; margin:16px 0; }
                .big-number .amount { font-size:32px; font-weight:700; color:#065f46; }
                .big-number .label  { font-size:12px; color:#6ee7b7; margin-top:4px; }
                .points-card { text-align:center; padding:20px; background:#eeedfe; border-radius:12px; margin:16px 0; }
                .points-card .pts   { font-size:36px; font-weight:700; color:#534AB7; }
                .points-card .label { font-size:12px; color:#7F77DD; margin-top:4px; }
                .password-display { background:#f5f3ff; border-radius:10px; padding:16px 20px; text-align:center; margin:16px 0; font-size:20px; font-weight:700; letter-spacing:4px; color:#534AB7; font-family:monospace; }
                .divider { border:none; border-top:1px solid #ede9fb; margin:24px 0; }
                .sign-off { font-size:14px; color:#555; line-height:1.7; }
                .footer { background:#f5f3ff; padding:16px 28px; text-align:center; font-size:12px; color:#999; border-top:1px solid #ede9fb; }
                ul.steps { padding-left:20px; color:#555; font-size:14px; line-height:2; margin:12px 0 16px; }
              </style>
            </head>
            <body>
              <div class="wrapper">
                <div class="container">
                  <div class="header">
                    <img src="cid:logo" alt="Waddler"/>
                    <span class="header-name">Waddler</span>
                  </div>
                  <div class="body">
            """;

    private final String emailFooter = """
                    <hr class="divider"/>
                    <p class="sign-off">Safe travels,<br/><strong>The Waddler Team</strong></p>
                  </div>
                  <div class="footer">
                    &copy; 2026 Waddler &nbsp;&middot;&nbsp;
                    <a href="mailto:waddler.info@gmail.com" style="color:#534AB7;text-decoration:none;">waddler.info@gmail.com</a>
                  </div>
                </div>
              </div>
            </body>
            </html>
            """;

    public void sendEmail(EmailDetails emailDetails) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(emailSender);
        helper.setTo(emailDetails.getRecipient());
        helper.setSubject(emailDetails.getSubject());

        MimeMultipart multipart = new MimeMultipart("related");

        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(emailHeader + emailDetails.getMessageBody() + emailFooter,
                "text/html; charset=UTF-8");
        multipart.addBodyPart(bodyPart);

        ClassPathResource logo = new ClassPathResource("static/waddler.png");
        try {
            if (logo.exists()) {
                MimeBodyPart imagePart = new MimeBodyPart();
                imagePart.setDataHandler(new DataHandler(new FileDataSource(logo.getFile())));
                imagePart.setHeader("Content-ID", "<logo>");
                imagePart.setHeader("Content-Disposition", "inline");
                multipart.addBodyPart(imagePart);
            }
        } catch (IOException e) {
            System.out.println("Warning: could not load waddler.png — " + e.getMessage());
        }

        mimeMessage.setContent(multipart);
        javaMailSender.send(mimeMessage);
    }

    private String roomLabel(org.example.rest.booking.Booking booking) {
        if (booking.getRoom() == null) return "N/A";
        String type = booking.getRoom().getRoomType() != null
                ? booking.getRoom().getRoomType().getDisplayName() : "";
        String name = booking.getRoom().getName() != null
                ? " — " + booking.getRoom().getName() : "";
        return type + name;
    }

    private String nightsLabel(org.example.rest.booking.Booking booking) {
        if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null) return "N/A";
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        return nights + (nights == 1 ? " night" : " nights");
    }

    private String policyBlock(org.example.rest.booking.Booking booking) {
        if (booking.getCancellationPolicyName() == null) return "";
        String name = booking.getCancellationPolicyName().getDisplayName();
        String days = booking.getCancellationDaysBeforeCheckin() != null
                ? "Cancel at least " + booking.getCancellationDaysBeforeCheckin() + " days before check-in" : "";
        String refund = booking.getCancellationRefundPercentage() != null
                ? booking.getCancellationRefundPercentage().stripTrailingZeros().toPlainString() + "% refund" : "";
        return """
                <div class="box box-purple">
                  <strong>Cancellation policy: %s</strong><br/>
                  %s &nbsp;·&nbsp; %s
                </div>""".formatted(name, days, refund);
    }

    public void sendWelcome(String toEmail, String firstName) throws MessagingException {
        String body = """
                <h2 class="greeting">Welcome to Waddler!</h2>
                <p class="text">Hello <strong>%s</strong>, your account has been created successfully. We're excited to have you on board!</p>
                <div class="box box-success">You can now search hotels, book rooms, reserve event tickets, and manage your stays — all in one place.</div>
                <p class="text">Start exploring hotels and plan your next adventure!</p>
                """.formatted(firstName);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Welcome to Waddler!")
                .body(body)
                .build());
    }

    public void sendNewPassword(String toEmail, String firstName, String newPassword)
            throws MessagingException {
        String body = """
                <h2 class="greeting">Password reset</h2>
                <p class="text">Hello <strong>%s</strong>, we received a request to reset your Waddler account password.</p>
                <p class="text" style="margin-bottom:6px;">Your new temporary password is:</p>
                <div class="password-display">%s</div>
                <div class="box box-warning">Please log in and <strong>change your password immediately</strong> from your account settings.</div>
                <p class="text">If you did not request this reset, please contact our support team right away.</p>
                """.formatted(firstName, newPassword);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Your New Temporary Password")
                .body(body)
                .build());
    }

    public void sendHotelManagerRegistrationUnderReview(String toEmail, String firstName)
            throws MessagingException {
        String body = """
                <h2 class="greeting">Registration received!</h2>
                <p class="text">Hello <strong>%s</strong>, thank you for registering as a Hotel Manager on Waddler!</p>
                <div class="box box-purple">Your account is currently <strong>under review</strong> by our team. We'll respond within <strong>1–3 business days</strong>.</div>
                <p class="text">Once approved, you'll receive a confirmation email and can log in to start adding your hotel.</p>
                <p class="text">Questions? Reach out at <a href="mailto:waddler.info@gmail.com" style="color:#534AB7;">waddler.info@gmail.com</a>.</p>
                """.formatted(firstName);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Your Hotel Manager Registration Is Under Review")
                .body(body)
                .build());
    }

    public void sendHotelManagerApproved(String toEmail, String firstName)
            throws MessagingException {
        String body = """
                <h2 class="greeting">You're approved!</h2>
                <p class="text">Hello <strong>%s</strong>, your Hotel Manager account on Waddler has been approved by our team.</p>
                <div class="box box-success">You can now log in and start setting up your hotel profile.</div>
                <p class="text" style="margin-bottom:6px;">Here's what you can do next:</p>
                <ul class="steps">
                  <li>Add your hotel information</li>
                  <li>Create rooms and set pricing</li>
                  <li>Define cancellation policies</li>
                  <li>Manage bookings and events</li>
                </ul>
                <p class="text">Welcome aboard — we look forward to a great partnership!</p>
                """.formatted(firstName);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Your Hotel Manager Account Is Approved!")
                .body(body)
                .build());
    }

    public void sendHotelManagerRejected(String toEmail, String firstName, String reason)
            throws MessagingException {
        String reasonHtml = (reason != null && !reason.isBlank())
                ? "<div class=\"box box-danger\"><strong>Reason:</strong> " + reason + "</div>"
                : "";

        String body = """
                <h2 class="greeting">Registration update</h2>
                <p class="text">Hello <strong>%s</strong>, after reviewing your Hotel Manager registration, we're unable to approve your account at this time.</p>
                %s
                <p class="text">If you believe this is a mistake, please contact us at <a href="mailto:waddler.info@gmail.com" style="color:#534AB7;">waddler.info@gmail.com</a>.</p>
                """.formatted(firstName, reasonHtml);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Hotel Manager Registration Update")
                .body(body)
                .build());
    }

    public void sendBookingPendingToUser(String toEmail, String firstName,
                                         org.example.rest.booking.Booking booking,
                                         String hotelName) throws MessagingException {
        String checkIn  = Objects.nonNull(booking.getCheckInDate())
                ? booking.getCheckInDate().toString() : "N/A";
        String checkOut = Objects.nonNull(booking.getCheckOutDate())
                ? booking.getCheckOutDate().toString() : "N/A";

        String body = """
                <h2 class="greeting">Booking received!</h2>
                <p class="text">Hello <strong>%s</strong>, we've received your booking request. The hotel will review and confirm it shortly.</p>
                <div class="nights-banner">%s &nbsp;·&nbsp; %s</div>
                <table class="info">
                  <tr><td>Booking ID</td><td><strong>#%d</strong></td></tr>
                  <tr><td>Hotel</td><td>%s</td></tr>
                  <tr><td>Room</td><td>%s</td></tr>
                  <tr><td>Check-in</td><td>%s</td></tr>
                  <tr><td>Check-out</td><td>%s</td></tr>
                  <tr><td>Guests</td><td>%d</td></tr>
                  <tr><td>Total amount</td><td><strong>$%.2f</strong></td></tr>
                  <tr><td>Status</td><td><span class="badge badge-pending">Pending confirmation</span></td></tr>
                </table>
                %s
                <p class="text">You'll receive another email once the hotel confirms your booking. Stay tuned!</p>
                """.formatted(firstName, nightsLabel(booking), hotelName,
                booking.getId(), hotelName, roomLabel(booking),
                checkIn, checkOut, booking.getNumberOfGuests(), booking.getTotalPrice(),
                policyBlock(booking));

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Booking #" + booking.getId() + " Received")
                .body(body)
                .build());
    }

    public void sendBookingPendingToManager(String toEmail, String managerFirstName,
                                            org.example.rest.booking.Booking booking,
                                            String hotelName, String guestName)
            throws MessagingException {
        String checkIn  = Objects.nonNull(booking.getCheckInDate())
                ? booking.getCheckInDate().toString() : "N/A";
        String checkOut = Objects.nonNull(booking.getCheckOutDate())
                ? booking.getCheckOutDate().toString() : "N/A";

        String body = """
                <h2 class="greeting">New booking request</h2>
                <p class="text">Hello <strong>%s</strong>, a new booking has been submitted for <strong>%s</strong> and is awaiting your confirmation.</p>
                <table class="info">
                  <tr><td>Booking ID</td><td><strong>#%d</strong></td></tr>
                  <tr><td>Guest</td><td>%s</td></tr>
                  <tr><td>Room</td><td>%s</td></tr>
                  <tr><td>Check-in</td><td>%s</td></tr>
                  <tr><td>Check-out</td><td>%s</td></tr>
                  <tr><td>Duration</td><td>%s</td></tr>
                  <tr><td>Guests</td><td>%d</td></tr>
                  <tr><td>Total</td><td><strong>$%.2f</strong></td></tr>
                </table>
                <p class="text">Please log in to the Waddler dashboard to <strong>confirm or cancel</strong> this booking.</p>
                """.formatted(managerFirstName, hotelName, booking.getId(), guestName,
                roomLabel(booking), checkIn, checkOut, nightsLabel(booking),
                booking.getNumberOfGuests(), booking.getTotalPrice());

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — New Booking #" + booking.getId() + " Needs Your Confirmation")
                .body(body)
                .build());
    }

    public void sendBookingConfirmation(String toEmail, String firstName,
                                        org.example.rest.booking.Booking booking,
                                        String hotelName) throws MessagingException {
        String checkIn  = Objects.nonNull(booking.getCheckInDate())
                ? booking.getCheckInDate().toString() : "N/A";
        String checkOut = Objects.nonNull(booking.getCheckOutDate())
                ? booking.getCheckOutDate().toString() : "N/A";

        String body = """
                <h2 class="greeting">Booking confirmed!</h2>
                <p class="text">Hello <strong>%s</strong>, great news — your booking has been confirmed. We look forward to welcoming you!</p>
                <div class="nights-banner">%s &nbsp;·&nbsp; %s</div>
                <table class="info">
                  <tr><td>Booking ID</td><td><strong>#%d</strong></td></tr>
                  <tr><td>Hotel</td><td>%s</td></tr>
                  <tr><td>Room</td><td>%s</td></tr>
                  <tr><td>Check-in</td><td>%s</td></tr>
                  <tr><td>Check-out</td><td>%s</td></tr>
                  <tr><td>Guests</td><td>%d</td></tr>
                  <tr><td>Total amount</td><td><strong>$%.2f</strong></td></tr>
                  <tr><td>Status</td><td><span class="badge badge-success">Confirmed</span></td></tr>
                </table>
                %s
                <div class="box box-purple">Your loyalty account has been credited with <strong>20 points</strong> for this booking.</div>
                """.formatted(firstName, nightsLabel(booking), hotelName,
                booking.getId(), hotelName, roomLabel(booking),
                checkIn, checkOut, booking.getNumberOfGuests(), booking.getTotalPrice(),
                policyBlock(booking));

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Booking #" + booking.getId() + " Confirmed!")
                .body(body)
                .build());
    }

    public void sendBookingCancellation(String toEmail, String firstName,
                                        org.example.rest.booking.Booking booking,
                                        String hotelName) throws MessagingException {
        String checkIn  = Objects.nonNull(booking.getCheckInDate())
                ? booking.getCheckInDate().toString() : "N/A";
        String checkOut = Objects.nonNull(booking.getCheckOutDate())
                ? booking.getCheckOutDate().toString() : "N/A";

        String body = """
                <h2 class="greeting">Booking cancelled</h2>
                <p class="text">Hello <strong>%s</strong>, your booking has been cancelled.</p>
                <table class="info">
                  <tr><td>Booking ID</td><td><strong>#%d</strong></td></tr>
                  <tr><td>Hotel</td><td>%s</td></tr>
                  <tr><td>Room</td><td>%s</td></tr>
                  <tr><td>Check-in</td><td>%s</td></tr>
                  <tr><td>Check-out</td><td>%s</td></tr>
                  <tr><td>Duration</td><td>%s</td></tr>
                  <tr><td>Guests</td><td>%d</td></tr>
                  <tr><td>Total</td><td>$%.2f</td></tr>
                  <tr><td>Status</td><td><span class="badge badge-danger">Cancelled</span></td></tr>
                </table>
                <p class="text">If you cancelled by mistake or need help, please contact our support team. We hope to see you again soon!</p>
                """.formatted(firstName, booking.getId(), hotelName, roomLabel(booking),
                checkIn, checkOut, nightsLabel(booking),
                booking.getNumberOfGuests(), booking.getTotalPrice());

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Booking #" + booking.getId() + " Cancelled")
                .body(body)
                .build());
    }

    public void sendPaymentConfirmation(String toEmail, String firstName,
                                        org.example.rest.payment.Payment payment,
                                        org.example.rest.booking.Booking booking,
                                        String hotelName) throws MessagingException {
        String checkIn  = Objects.nonNull(booking.getCheckInDate())
                ? booking.getCheckInDate().toString() : "N/A";
        String checkOut = Objects.nonNull(booking.getCheckOutDate())
                ? booking.getCheckOutDate().toString() : "N/A";
        String method = Objects.nonNull(payment.getPaymentMethod())
                ? payment.getPaymentMethod().getDisplayName() : "N/A";
        String transactionId = Objects.nonNull(payment.getTransactionId())
                ? payment.getTransactionId() : "N/A";

        String body = """
                <h2 class="greeting">Payment confirmed!</h2>
                <p class="text">Hello <strong>%s</strong>, your payment has been received successfully.</p>
                <div class="big-number">
                  <div class="amount">$%.2f</div>
                  <div class="label">payment received</div>
                </div>
                <table class="info">
                  <tr><td>Transaction ID</td><td>%s</td></tr>
                  <tr><td>Payment method</td><td>%s</td></tr>
                  <tr><td>Hotel</td><td>%s</td></tr>
                  <tr><td>Room</td><td>%s</td></tr>
                  <tr><td>Check-in</td><td>%s</td></tr>
                  <tr><td>Check-out</td><td>%s</td></tr>
                  <tr><td>Duration</td><td>%s</td></tr>
                  <tr><td>Guests</td><td>%d</td></tr>
                  <tr><td>Amount paid</td><td><strong>$%.2f</strong></td></tr>
                </table>
                <p class="text">Keep this email as your payment receipt. Have a wonderful stay!</p>
                """.formatted(firstName, payment.getAmount(),
                transactionId, method, hotelName, roomLabel(booking),
                checkIn, checkOut, nightsLabel(booking),
                booking.getNumberOfGuests(), payment.getAmount());

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Payment Confirmed for Booking #" + booking.getId())
                .body(body)
                .build());
    }

    public void sendCancellationPolicyViolation(String toEmail, String firstName,
                                                org.example.rest.booking.Booking booking,
                                                String hotelName, long daysUntilCheckIn)
            throws MessagingException {
        String checkIn = Objects.nonNull(booking.getCheckInDate())
                ? booking.getCheckInDate().toString() : "N/A";
        String policyName = Objects.nonNull(booking.getCancellationPolicyName())
                ? booking.getCancellationPolicyName().getDisplayName() : "N/A";
        int required = Objects.nonNull(booking.getCancellationDaysBeforeCheckin())
                ? booking.getCancellationDaysBeforeCheckin() : 0;

        String body = """
                <h2 class="greeting">Cancellation request denied</h2>
                <p class="text">Hello <strong>%s</strong>, unfortunately your cancellation request could not be processed because it violates the cancellation policy.</p>
                <div class="box box-danger">The <strong>%s</strong> policy requires cancellation at least <strong>%d days</strong> before check-in. Your check-in is in <strong>%d days</strong>.</div>
                <table class="info">
                  <tr><td>Hotel</td><td>%s</td></tr>
                  <tr><td>Check-in</td><td>%s</td></tr>
                  <tr><td>Days until check-in</td><td>%d days</td></tr>
                  <tr><td>Policy</td><td>%s</td></tr>
                  <tr><td>Required notice</td><td>%d days before check-in</td></tr>
                </table>
                <p class="text">If you have any questions, please contact our support team.</p>
                """.formatted(firstName, policyName, required, daysUntilCheckIn,
                hotelName, checkIn, daysUntilCheckIn, policyName, required);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Cancellation Request Denied for Booking #" + booking.getId())
                .body(body)
                .build());
    }

    public void sendRefundConfirmation(String toEmail, String firstName,
                                       org.example.rest.booking.Booking booking,
                                       String hotelName) throws MessagingException {
        String refundAmount = Objects.nonNull(booking.getRefundAmount())
                ? "$" + booking.getRefundAmount().toPlainString() : "$0.00";
        String checkIn = Objects.nonNull(booking.getCheckInDate())
                ? booking.getCheckInDate().toString() : "N/A";
        double total = Objects.nonNull(booking.getTotalPrice()) ? booking.getTotalPrice() : 0.0;

        String body = """
                <h2 class="greeting">Refund confirmed!</h2>
                <p class="text">Hello <strong>%s</strong>, your booking has been cancelled and a refund has been processed.</p>
                <div class="big-number">
                  <div class="amount">%s</div>
                  <div class="label">refund processed</div>
                </div>
                <table class="info">
                  <tr><td>Hotel</td><td>%s</td></tr>
                  <tr><td>Room</td><td>%s</td></tr>
                  <tr><td>Check-in</td><td>%s</td></tr>
                  <tr><td>Original amount</td><td>$%.2f</td></tr>
                  <tr><td>Refund amount</td><td><strong style="color:#065f46;">%s</strong></td></tr>
                </table>
                <p class="text">Your refund will be processed to your original payment method within <strong>5–7 business days</strong>.</p>
                """.formatted(firstName, refundAmount, hotelName, roomLabel(booking),
                checkIn, total, refundAmount);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Refund Confirmed for Booking #" + booking.getId())
                .body(body)
                .build());
    }

    public void sendRoomDeactivated(String toEmail, String firstName,
                                    org.example.rest.booking.Booking booking,
                                    String hotelName, String roomName, String managerEmail)
            throws MessagingException {
        String checkIn  = Objects.nonNull(booking.getCheckInDate())
                ? booking.getCheckInDate().toString() : "N/A";
        String checkOut = Objects.nonNull(booking.getCheckOutDate())
                ? booking.getCheckOutDate().toString() : "N/A";

        String body = """
                <h2 class="greeting">Important notice</h2>
                <p class="text">Hello <strong>%s</strong>, the room you booked has been temporarily deactivated by the hotel manager. We sincerely apologize for the inconvenience.</p>
                <table class="info">
                  <tr><td>Hotel</td><td>%s</td></tr>
                  <tr><td>Room</td><td>%s</td></tr>
                  <tr><td>Check-in</td><td>%s</td></tr>
                  <tr><td>Check-out</td><td>%s</td></tr>
                  <tr><td>Duration</td><td>%s</td></tr>
                </table>
                <div class="box box-warning">Please contact the hotel manager at <strong>%s</strong> to arrange an alternative room.</div>
                <p class="text">We are committed to ensuring you have a great stay and will do our best to resolve this quickly.</p>
                """.formatted(firstName, hotelName, roomName, checkIn, checkOut,
                nightsLabel(booking), managerEmail);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Important Notice: Your Room Has Been Deactivated")
                .body(body)
                .build());
    }

    public void sendEventReservationConfirmation(String toEmail, String firstName,
                                                 org.example.rest.eventreservation.EventReservation reservation)
            throws MessagingException {
        org.example.rest.event.Event event = reservation.getEvent();
        String eventTitle    = event != null ? event.getTitle() : "N/A";
        String eventType     = event != null && event.getCategory() != null
                ? event.getCategory().getDisplayName() : "N/A";
        String eventDate     = event != null && event.getStartDateTime() != null
                ? event.getStartDateTime().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' hh:mm a")) : "N/A";
        String eventEnd      = event != null && event.getEndDateTime() != null
                ? event.getEndDateTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : "N/A";
        String hotelName     = event != null && event.getHotel() != null
                ? event.getHotel().getName() : "N/A";
        String location      = event != null && event.getAddress() != null
                ? event.getAddress() + (event.getCity() != null ? ", " + event.getCity() : "") : hotelName;
        String refundPolicy  = event != null && Boolean.TRUE.equals(event.getRefundEnabled())
                ? event.getRefundPercent() + "% refund if cancelled more than 1 hour before the event"
                : "No refund — cancellation not allowed within 1 hour of the event";

        String body = """
                <h2 class="greeting">Event reservation confirmed!</h2>
                <p class="text">Hello <strong>%s</strong>, your event reservation has been confirmed. We can't wait to see you there!</p>
                <table class="info">
                  <tr><td>Reservation code</td><td><strong>%s</strong></td></tr>
                  <tr><td>Event</td><td><strong>%s</strong></td></tr>
                  <tr><td>Event type</td><td>%s</td></tr>
                  <tr><td>Date &amp; time</td><td>%s – %s</td></tr>
                  <tr><td>Venue</td><td>%s</td></tr>
                  <tr><td>Participants</td><td>%d</td></tr>
                  <tr><td>Total amount</td><td><strong>$%.2f</strong></td></tr>
                  <tr><td>Status</td><td><span class="badge badge-success">Confirmed</span></td></tr>
                </table>
                %s
                <div class="box box-purple">
                  <strong>Cancellation policy</strong><br/>
                  %s
                </div>
                <p class="text">Please keep this reservation code for check-in at the event.</p>
                """.formatted(firstName,
                reservation.getReservationCode(), eventTitle, eventType,
                eventDate, eventEnd, location,
                reservation.getParticipantsCount(), reservation.getTotalAmount(),
                reservation.getMeetingPoint() != null
                        ? "<div class=\"box box-warning\"><strong>Meeting point:</strong> " + reservation.getMeetingPoint() + "</div>"
                        : "",
                refundPolicy);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Event Reservation " + reservation.getReservationCode() + " Confirmed!")
                .body(body)
                .build());
    }

    public void sendEventReservationCancellation(String toEmail, String firstName,
                                                 org.example.rest.eventreservation.EventReservation reservation)
            throws MessagingException {
        org.example.rest.event.Event event = reservation.getEvent();
        String eventTitle = event != null ? event.getTitle() : "N/A";
        String eventType  = event != null && event.getCategory() != null
                ? event.getCategory().getDisplayName() : "N/A";
        String eventDate  = event != null && event.getStartDateTime() != null
                ? event.getStartDateTime().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' hh:mm a")) : "N/A";
        String refundNote = event != null && Boolean.TRUE.equals(event.getRefundEnabled())
                ? "<div class=\"box box-success\">A refund of <strong>" + event.getRefundPercent()
                + "%</strong> of your total ($" + reservation.getTotalAmount() + ") will be processed within 5–7 business days.</div>"
                : "<div class=\"box box-warning\">This event is non-refundable. No refund will be issued for this cancellation.</div>";

        String body = """
                <h2 class="greeting">Event reservation cancelled</h2>
                <p class="text">Hello <strong>%s</strong>, your event reservation has been cancelled.</p>
                <table class="info">
                  <tr><td>Reservation code</td><td>%s</td></tr>
                  <tr><td>Event</td><td><strong>%s</strong></td></tr>
                  <tr><td>Event type</td><td>%s</td></tr>
                  <tr><td>Date &amp; time</td><td>%s</td></tr>
                  <tr><td>Participants</td><td>%d</td></tr>
                  <tr><td>Total paid</td><td>$%.2f</td></tr>
                  <tr><td>Status</td><td><span class="badge badge-danger">Cancelled</span></td></tr>
                </table>
                %s
                <p class="text">If you cancelled by mistake, please contact our support team. We hope to see you at a future event!</p>
                """.formatted(firstName,
                reservation.getReservationCode(), eventTitle, eventType, eventDate,
                reservation.getParticipantsCount(), reservation.getTotalAmount(),
                refundNote);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Event Reservation " + reservation.getReservationCode() + " Cancelled")
                .body(body)
                .build());
    }

    public void sendTableReservationConfirmation(String toEmail, String firstName,
                                                 org.example.rest.tablereservation.TableReservation reservation)
            throws MessagingException {
        String hotelName = reservation.getHotel() != null
                ? reservation.getHotel().getName() : "N/A";
        String dateTime  = Objects.nonNull(reservation.getReservationDateTime())
                ? reservation.getReservationDateTime().format(
                DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' hh:mm a")) : "N/A";
        String occasion  = reservation.getSpecialOccasion() != null
                ? reservation.getSpecialOccasion().getDisplayName() : null;

        String body = """
                <h2 class="greeting">Table reservation confirmed!</h2>
                <p class="text">Hello <strong>%s</strong>, your table reservation has been confirmed. We look forward to hosting you!</p>
                <table class="info">
                  <tr><td>Reservation code</td><td><strong>%s</strong></td></tr>
                  <tr><td>Restaurant</td><td>%s</td></tr>
                  <tr><td>Date &amp; time</td><td>%s</td></tr>
                  <tr><td>Guests</td><td>%d</td></tr>
                  <tr><td>Duration</td><td>%d minutes</td></tr>
                  %s
                  <tr><td>Status</td><td><span class="badge badge-success">Confirmed</span></td></tr>
                </table>
                %s
                <div class="box box-purple">
                  <strong>Cancellation policy</strong><br/>
                  Cancellation is free if done more than 1 hour before your reservation time. Cancellations within 1 hour are not permitted.
                </div>
                <p class="text">Please arrive on time. If your plans change, cancel as soon as possible.</p>
                """.formatted(firstName,
                reservation.getReservationCode(), hotelName, dateTime,
                reservation.getGuestCount(), reservation.getDurationMinutes(),
                occasion != null ? "<tr><td>Special occasion</td><td>" + occasion + "</td></tr>" : "",
                reservation.getDietaryRestrictions() != null
                        ? "<div class=\"box box-warning\"><strong>Dietary restrictions noted:</strong> "
                        + reservation.getDietaryRestrictions() + "</div>"
                        : "");

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Table Reservation " + reservation.getReservationCode() + " Confirmed!")
                .body(body)
                .build());
    }

    public void sendTableReservationCancellation(String toEmail, String firstName,
                                                 org.example.rest.tablereservation.TableReservation reservation)
            throws MessagingException {
        String hotelName = reservation.getHotel() != null
                ? reservation.getHotel().getName() : "N/A";
        String dateTime  = Objects.nonNull(reservation.getReservationDateTime())
                ? reservation.getReservationDateTime().format(
                DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' hh:mm a")) : "N/A";

        String body = """
                <h2 class="greeting">Table reservation cancelled</h2>
                <p class="text">Hello <strong>%s</strong>, your table reservation has been cancelled.</p>
                <table class="info">
                  <tr><td>Reservation code</td><td>%s</td></tr>
                  <tr><td>Restaurant</td><td>%s</td></tr>
                  <tr><td>Date &amp; time</td><td>%s</td></tr>
                  <tr><td>Guests</td><td>%d</td></tr>
                  <tr><td>Status</td><td><span class="badge badge-danger">Cancelled</span></td></tr>
                </table>
                <p class="text">We hope to welcome you another time. Feel free to make a new reservation whenever you're ready!</p>
                """.formatted(firstName,
                reservation.getReservationCode(), hotelName, dateTime,
                reservation.getGuestCount());

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Table Reservation " + reservation.getReservationCode() + " Cancelled")
                .body(body)
                .build());
    }

    public void sendContactConfirmation(String toEmail, String senderName,
                                        org.example.rest.contact.Contact contact)
            throws MessagingException {
        String submitted = Objects.nonNull(contact.getCreatedAt())
                ? contact.getCreatedAt().format(DATE_FORMATTER) : "N/A";

        String body = """
                <h2 class="greeting">We've received your message!</h2>
                <p class="text">Hello <strong>%s</strong>, thank you for reaching out. We've received your message and will get back to you as soon as possible.</p>
                <table class="info">
                  <tr><td>Subject</td><td>%s</td></tr>
                  <tr><td>Category</td><td>%s</td></tr>
                  <tr><td>Submitted</td><td>%s</td></tr>
                </table>
                <p class="text">Our team typically responds within <strong>24–48 hours</strong>.</p>
                """.formatted(senderName, contact.getSubject(),
                contact.getCategory().getDisplayName(), submitted);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — We received your message!")
                .body(body)
                .build());
    }

    public void sendContactNotificationToAdmin(String toEmail, String adminFirstName,
                                               org.example.rest.contact.Contact contact)
            throws MessagingException {
        String body = """
                <h2 class="greeting">New contact request</h2>
                <p class="text">Hello <strong>%s</strong>, a new contact request requires your attention.</p>
                <table class="info">
                  <tr><td>Reference ID</td><td>#%d</td></tr>
                  <tr><td>From</td><td>%s (%s)</td></tr>
                  <tr><td>Subject</td><td>%s</td></tr>
                  <tr><td>Category</td><td>%s</td></tr>
                  <tr><td>Message</td><td>%s</td></tr>
                </table>
                <p class="text">Please log in to the Waddler dashboard to review and respond.</p>
                """.formatted(adminFirstName, contact.getId(),
                contact.getSenderName(), contact.getSenderEmail(),
                contact.getSubject(), contact.getCategory().getDisplayName(),
                contact.getMessage());

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — New Contact Request [" + contact.getCategory().getDisplayName() + "]")
                .body(body)
                .build());
    }

    public void sendContactResolution(String toEmail, String senderName,
                                      org.example.rest.contact.Contact contact)
            throws MessagingException {
        String resolvedOn = Objects.nonNull(contact.getResolvedAt())
                ? contact.getResolvedAt().format(DATE_FORMATTER) : "N/A";

        String body = """
                <h2 class="greeting">Your request has been resolved</h2>
                <p class="text">Hello <strong>%s</strong>, we've reviewed your contact request and have a response for you.</p>
                <table class="info">
                  <tr><td>Subject</td><td>%s</td></tr>
                  <tr><td>Resolved on</td><td>%s</td></tr>
                  <tr><td>Status</td><td><span class="badge badge-success">Resolved</span></td></tr>
                </table>
                <p class="text" style="margin-bottom:6px;"><strong>Our response:</strong></p>
                <div class="box box-purple">%s</div>
                <p class="text">If you have further questions, feel free to reach out again.</p>
                """.formatted(senderName, contact.getSubject(), resolvedOn,
                contact.getResolutionMessage());

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — We've responded to your request")
                .body(body)
                .build());
    }

    public void sendEventCancelledByManager(String toEmail, String firstName,
                                            org.example.rest.event.Event event,
                                            java.util.List<org.example.rest.event.Event> otherEvents)
            throws MessagingException {
        String eventTitle = event.getTitle() != null ? event.getTitle() : "N/A";
        String eventType  = event.getCategory() != null ? event.getCategory().getDisplayName() : "N/A";
        String eventDate  = event.getStartDateTime() != null
                ? event.getStartDateTime().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' hh:mm a"))
                : "N/A";
        String hotelName  = event.getHotel() != null ? event.getHotel().getName() : "N/A";

        StringBuilder suggestions = new StringBuilder();
        if (!otherEvents.isEmpty()) {
            suggestions.append("<p class=\"text\" style=\"margin-bottom:6px;\"><strong>Explore other available events:</strong></p>");
            suggestions.append("<table class=\"info\">");
            for (org.example.rest.event.Event e : otherEvents) {
                String date = e.getStartDateTime() != null
                        ? e.getStartDateTime().format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' hh:mm a"))
                        : "TBD";
                String price = e.getPrice() != null && e.getPrice() > 0
                        ? "$" + e.getPrice() : "Free";
                suggestions.append(String.format(
                        "<tr><td><strong>%s</strong></td><td>%s &nbsp;·&nbsp; %s</td></tr>",
                        e.getTitle(), date, price));
            }
            suggestions.append("</table>");
        }

        String body = """
                <h2 class="greeting">Event cancelled</h2>
                <p class="text">Hello <strong>%s</strong>, we regret to inform you that the following event has been cancelled by the hotel. We sincerely apologize for the inconvenience.</p>
                <table class="info">
                  <tr><td>Event</td><td><strong>%s</strong></td></tr>
                  <tr><td>Type</td><td>%s</td></tr>
                  <tr><td>Date &amp; time</td><td>%s</td></tr>
                  <tr><td>Venue</td><td>%s</td></tr>
                </table>
                <div class="box box-success">A <strong>full refund</strong> will be processed to your original payment method within <strong>5–7 business days</strong>.</div>
                %s
                <p class="text">We hope to see you at a future event. Thank you for your understanding!</p>
                """.formatted(firstName, eventTitle, eventType, eventDate, hotelName, suggestions);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Event Cancelled: " + eventTitle)
                .body(body)
                .build());
    }

    public void sendTableCancelledByManager(String toEmail, String firstName,
                                            org.example.rest.tablereservation.TableReservation reservation,
                                            java.util.List<org.example.rest.tablereservation.TableReservation> otherTables)
            throws MessagingException {
        String hotelName = reservation.getHotel() != null ? reservation.getHotel().getName() : "N/A";
        String dateTime  = reservation.getReservationDateTime() != null
                ? reservation.getReservationDateTime().format(
                DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' hh:mm a"))
                : "N/A";

        StringBuilder suggestions = new StringBuilder();
        if (!otherTables.isEmpty()) {
            suggestions.append("<p class=\"text\" style=\"margin-bottom:6px;\"><strong>Available tables at this restaurant:</strong></p>");
            suggestions.append("<table class=\"info\">");
            for (org.example.rest.tablereservation.TableReservation t : otherTables) {
                String dt = t.getReservationDateTime().format(
                        DateTimeFormatter.ofPattern("MMM d, yyyy 'at' hh:mm a"));
                String type = t.getTableType() != null
                        ? t.getTableType().getDisplayName() : "Standard";
                suggestions.append(String.format(
                        "<tr><td><strong>%s table</strong></td><td>%s &nbsp;·&nbsp; up to %d guests</td></tr>",
                        type, dt, t.getGuestCount()));
            }
            suggestions.append("</table>");
        }

        String body = """
                <h2 class="greeting">Table reservation cancelled</h2>
                <p class="text">Hello <strong>%s</strong>, we regret to inform you that your table reservation has been cancelled by the hotel. We sincerely apologize for the inconvenience.</p>
                <table class="info">
                  <tr><td>Reservation code</td><td><strong>%s</strong></td></tr>
                  <tr><td>Restaurant</td><td>%s</td></tr>
                  <tr><td>Date &amp; time</td><td>%s</td></tr>
                  <tr><td>Guests</td><td>%d</td></tr>
                </table>
                <div class="box box-success">A <strong>full refund</strong> will be processed to your original payment method within <strong>5–7 business days</strong>.</div>
                %s
                <p class="text">We hope to welcome you another time. Thank you for your understanding!</p>
                """.formatted(firstName, reservation.getReservationCode(), hotelName,
                dateTime, reservation.getGuestCount(), suggestions);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Table Reservation " + reservation.getReservationCode() + " Cancelled")
                .body(body)
                .build());
    }

    public void sendManagerCancellationRefund(String toEmail, String firstName,
                                              String itemDescription,
                                              java.math.BigDecimal amount) throws MessagingException {

        String amountBlock = amount != null && amount.compareTo(java.math.BigDecimal.ZERO) > 0
                ? """
                  <div class="big-number">
                    <div class="amount">$%.2f</div>
                    <div class="label">full refund issued</div>
                  </div>""".formatted(amount)
                : "<div class=\"box box-success\">No charge was applied — nothing to refund.</div>";

        String refundRow = amount != null && amount.compareTo(java.math.BigDecimal.ZERO) > 0
                ? "<tr><td>Refund amount</td><td><strong style=\"color:#065f46;\">$%.2f</strong></td></tr>".formatted(amount)
                : "<tr><td>Charge</td><td>None</td></tr>";

        String body = """
                <h2 class="greeting">Cancellation notice</h2>
                <p class="text">Hello <strong>%s</strong>, your reservation has been cancelled by the hotel. Here is a summary of any charges.</p>
                %s
                <table class="info">
                  <tr><td>Reservation</td><td>%s</td></tr>
                  %s
                  <tr><td>Reason</td><td>Cancelled by hotel</td></tr>
                </table>
                <p class="text">We're sorry for any inconvenience this may have caused. Please don't hesitate to reach out if you have questions.</p>
                """.formatted(firstName, amountBlock, itemDescription, refundRow);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Full Refund Processed")
                .body(body)
                .build());
    }

    public void sendPasswordResetOtp(String toEmail, String firstName, String otp)
            throws MessagingException {
        String body = """
                <h2 class="greeting">Password reset request</h2>
                <p class="text">Hello <strong>%s</strong>, we received a request to reset your Waddler account password.</p>
                <p class="text" style="margin-bottom:6px;">Your one-time password (OTP) is:</p>
                <div class="password-display">%s</div>
                <div class="box box-warning">This OTP is valid for <strong>10 minutes</strong>. Do not share it with anyone.</div>
                <p class="text">If you did not request a password reset, you can safely ignore this email.</p>
                """.formatted(firstName, otp);

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — Your Password Reset OTP")
                .body(body)
                .build());
    }

    public void sendNewManagerNotificationToAdmin(String toEmail, String adminFirstName,
                                                  org.example.rest.security.user.AppUser manager)
            throws MessagingException {
        String body = """
                <h2 class="greeting">New manager registration</h2>
                <p class="text">Hello <strong>%s</strong>, a new hotel manager has registered and is awaiting your approval.</p>
                <table class="info">
                  <tr><td>Name</td><td><strong>%s %s</strong></td></tr>
                  <tr><td>Email</td><td>%s</td></tr>
                </table>
                <p class="text">Please review and approve or reject this registration from the admin panel.</p>
                """.formatted(adminFirstName,
                manager.getFirstName(), manager.getLastName(),
                manager.getEmail());

        sendEmail(EmailMessageBuilder.builder()
                .to(toEmail)
                .subject("Waddler — New Hotel Manager Awaiting Approval")
                .body(body)
                .build());
    }

}