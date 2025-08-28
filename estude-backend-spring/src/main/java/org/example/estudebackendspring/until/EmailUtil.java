package org.example.estudebackendspring.until;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@Slf4j
public class EmailUtil {

    @Autowired
    private JavaMailSender javaMailSender;

    // Thư gửi mặc định (tốt nhất set bằng spring.mail.username)
    @Value("${spring.mail.username}")
    private String mailFrom;

    // Thời hạn OTP để hiển thị trong email (ví dụ 15 phút)
    @Value("${app.otp.expire-minutes:15}")
    private long otpExpireMinutes;

    /**
     * Gửi OTP 6 chữ số trong body (KHÔNG đặt trực tiếp token trong query string).
     * Frontend/UX: user sẽ nhận mã và dán vào form "Enter OTP".
     */
    public void sendOtpEmail(String email, String otp) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        // set multipart = false, nhưng dùng helper với UTF-8
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

        helper.setTo(email);
        helper.setFrom(mailFrom);
        helper.setReplyTo(mailFrom);
        helper.setSubject("Mã OTP xác thực tài khoản");
        String html = """
            <div style="font-family: Arial, sans-serif; line-height:1.4;">
              <p>Xin chào,</p>
              <p>Bạn đã yêu cầu đặt lại mật khẩu / xác thực tài khoản.</p>
              <p><strong>Mã OTP của bạn là: <span style="font-size:20px;">%s</span></strong></p>
              <p>Mã này có hiệu lực trong <strong>%d phút</strong>. Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>
              <p>Trân trọng,<br/>Đội ngũ hỗ trợ</p>
            </div>
            """.formatted(otp, otpExpireMinutes);

        helper.setText(html, true);
        javaMailSender.send(mimeMessage);

        // KHÔNG log OTP ở môi trường production; log chỉ thông tin non-sensitive nếu cần
        log.info("Sent OTP email to {}", email);
    }

    /**
     * Gửi link reset password (nếu bạn vẫn muốn link).
     * LƯU Ý: token trong URL có thể bị lưu trữ ở history/proxy logs.
     * Tốt hơn là gửi chỉ token trong body và yêu cầu user nhập token.
     */
    public void sendResetPasswordEmail(String email, String resetToken) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(email);
        helper.setFrom(mailFrom);
        helper.setReplyTo(mailFrom);
        helper.setSubject("Yêu cầu đặt lại mật khẩu");

        // Gợi ý: kèm cả token hiển thị + link frontend (frontend sẽ lấy token từ body hoặc từ query)
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;

        String html = """
            <div style="font-family: Arial, sans-serif; line-height:1.4;">
              <p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
              <p><strong>Mã đặt lại của bạn:</strong> <span style="font-size:16px;">%s</span></p>
              <p>Bạn cũng có thể nhấn vào link dưới đây để tới trang đặt lại mật khẩu:</p>
              <p><a href="%s" target="_blank">Reset password</a></p>
              <p>Mã/Link này có hiệu lực trong <strong>%d phút</strong>. Nếu bạn không yêu cầu, vui lòng bỏ qua.</p>
              <p>Trân trọng,<br/>Đội ngũ hỗ trợ</p>
            </div>
            """.formatted(resetToken, resetLink, otpExpireMinutes);

        helper.setText(html, true);
        javaMailSender.send(mimeMessage);
        log.info("Sent reset-password email to {}", email);
    }
}
