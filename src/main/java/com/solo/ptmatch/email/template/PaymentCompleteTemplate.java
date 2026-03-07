package com.solo.ptmatch.email.template;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentCompleteTemplate {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public static String build(String customerName, String orderId,
                                Integer amount, LocalDateTime approvedAt) {
        return """
                <div style="font-family:'Apple SD Gothic Neo','Malgun Gothic',sans-serif;\
                max-width:600px;margin:0 auto;">
                  <div style="background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);\
                padding:40px;border-radius:12px 12px 0 0;text-align:center;">
                    <h1 style="color:#fff;margin:0;">✅ 결제가 완료되었습니다</h1>
                  </div>
                  <div style="background:#fff;padding:32px;border:1px solid #e5e7eb;\
                border-radius:0 0 12px 12px;">
                    <p style="font-size:16px;">안녕하세요, <strong>%s</strong>님!</p>
                    <p>PT 매칭 결제가 정상적으로 완료되었습니다.</p>
                    <table style="width:100%%;border-collapse:collapse;margin:24px 0;">
                      <tr style="border-bottom:1px solid #f3f4f6;">
                        <td style="padding:12px 0;color:#6b7280;">주문번호</td>
                        <td style="padding:12px 0;text-align:right;font-weight:600;">%s</td>
                      </tr>
                      <tr style="border-bottom:1px solid #f3f4f6;">
                        <td style="padding:12px 0;color:#6b7280;">결제금액</td>
                        <td style="padding:12px 0;text-align:right;font-weight:600;">%,d원</td>
                      </tr>
                      <tr>
                        <td style="padding:12px 0;color:#6b7280;">결제일시</td>
                        <td style="padding:12px 0;text-align:right;font-weight:600;">%s</td>
                      </tr>
                    </table>
                    <p style="color:#6b7280;font-size:14px;">
                      트레이너가 매칭을 수락하면 별도로 안내드리겠습니다.
                    </p>
                  </div>
                </div>
                """.formatted(customerName, orderId, amount, approvedAt.format(FORMATTER));
    }
}
