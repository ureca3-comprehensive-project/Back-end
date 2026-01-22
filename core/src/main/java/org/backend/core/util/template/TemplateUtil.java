package org.backend.core.util.template;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.backend.core.dto.InvoiceDetailDto;
import org.backend.core.dto.InvoiceDto;
import org.backend.core.dto.TemplateDto;
import org.springframework.stereotype.Component;

@Component
public class TemplateUtil {
	
	private static final Pattern VARIABLE_PATTERN =
            Pattern.compile("\\{\\{(.*?)}}");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final int MAX_DETAIL_LINES = 5;

    private TemplateUtil() {
    }

    /**
     * 템플릿 문자열 렌더링 ({{key}} 치환)
     */
    public static String render(String templateText, Map<String, Object> payload) {
        Matcher matcher = VARIABLE_PATTERN.matcher(templateText);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object value = payload.getOrDefault(key, "");
            matcher.appendReplacement(
                    result,
                    Matcher.quoteReplacement(String.valueOf(value))
            );
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 제목 + 본문 렌더링
     */
    public static RenderedTemplate renderTemplate(
            TemplateDto template,
            Map<String, Object> payload
    ) {
        return new RenderedTemplate(
                render(template.getTitle(), payload),
                render(template.getBody(), payload)
        );
    }

    /**
     * Invoice / InvoiceDetail → Template Payload 생성
     */
    public static Map<String, Object> extractInvoicePayload(
            InvoiceDto invoice,
            List<InvoiceDetailDto> details
    ) {
        Map<String, Object> payload = new HashMap<>();

        payload.put("invoiceId", invoice.getId());
        payload.put("billingMonth", invoice.getBillingMonth());
        payload.put("totalAmount", invoice.getTotalAmount());
        payload.put("phone", invoice.getPhone());
        payload.put(
                "dueDate",
                invoice.getDueDate().format(DATE_FORMAT)
        );

        // InvoiceDetail 집계 (billingType + 상태(+/-))
        payload.put("baseFeeAmount", sumByType(details, "기본 요금"));
        payload.put("overUsageAmount", sumByType(details, "초과 과금"));
        payload.put("addOnAmount", sumByType(details, "부가서비스"));
        payload.put("discountAmount", sumByType(details, "할인 요금"));

        // EMAIL 전용 상세 요약
        payload.put("detailCount", details.size());
        payload.put("detailSummary", buildDetailSummary(details));

        return payload;
    }

    /**
     * billingType 기준 금액 합산
     */
    private static long sumByType(
            List<InvoiceDetailDto> details,
            String billingType
    ) {
        return details.stream()
                .filter(d -> d.getBillingType().equals(billingType))
                .mapToLong(d ->
                        d.isPositive()
                                ? d.getAmount()
                                : -d.getAmount()
                )
                .sum();
    }

    /**
     * 다건 InvoiceDetail 요약 문자열 생성 (EMAIL 전용)
     */
    private static String buildDetailSummary(List<InvoiceDetailDto> details) {
        if (details.isEmpty()) {
            return "- 청구 내역이 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        int limit = Math.min(details.size(), MAX_DETAIL_LINES);

        for (int i = 0; i < limit; i++) {
            InvoiceDetailDto detail = details.get(i);
            sb.append("- ")
              .append(detail.getBillingType())
              .append(" : ")
              .append(detail.isPositive() ? "" : "-")
              .append(detail.getAmount())
              .append("원\n");
        }

        if (details.size() > MAX_DETAIL_LINES) {
            sb.append("외 ")
              .append(details.size() - MAX_DETAIL_LINES)
              .append("건의 청구 내역이 있습니다.\n");
        }

        return sb.toString();
    }

    /**
     * 렌더링 결과 DTO
     */
    public record RenderedTemplate(
            String title,
            String body
    ) {
    }

}
