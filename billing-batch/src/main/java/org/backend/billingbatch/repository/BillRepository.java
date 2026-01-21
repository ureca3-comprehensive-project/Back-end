package org.backend.billingbatch.repository;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BillRepository {

    private final JdbcTemplate jdbcTemplate;

    public BigDecimal getUsageSum(Long lineId, String itemType, String logMonth){
        String sql =
                """
                SELECT used_amount
                FROM UsageLog
                WHERE line_id = ? AND item_type = ? AND log_month = ?;
                """;
        return jdbcTemplate.queryForObject(sql,BigDecimal.class, lineId, itemType, logMonth);
    }

    public BigDecimal getVasSum(Long lineId){
        String sql =
                """
                SELECT COALESCE(SUM(v.monthly_price),0)
                FROM LineVasSubscription lvs
                JOIN Vas v on lvs.vas_id = v.vas_id
                where lvs.line_id = ?;
                """;

        return jdbcTemplate.queryForObject(sql,BigDecimal.class, lineId);
    }


}
