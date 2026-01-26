package org.backend.billingbatch.dummy;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CsvLoadService {
	
	private final JdbcTemplate jdbc;
    private final ResourceLoader resourceLoader;
	
	public void loadMasterData() {
		log.info("[Start Insert - settings.sql]");
        executeSqlFile("sql/setting.sql");
    }
	
    public void loadUser() {
    	log.info("[Start Insert - Users.csv]");
//    	load("/var/lib/mysql-files/User.csv", "users");
    	String sql = """
			LOAD DATA INFILE '/var/lib/mysql-files/Users.csv'
			INTO TABLE users
			FIELDS TERMINATED BY ','
			OPTIONALLY ENCLOSED BY '"'
			LINES TERMINATED BY '\n'
			IGNORE 1 ROWS
			(
			  user_id,
			  created_at,
			  updated_at,
			  email,
			  name,
			  status
			)
    		""";

    			jdbc.execute(sql);
    }
    
    public void loadBanTime() {
    	log.info("[Start Insert - BanTime.csv]");

        String sql = """
        	    LOAD DATA INFILE '/var/lib/mysql-files/BanTime.csv'
        	    INTO TABLE ban_time
        	    FIELDS TERMINATED BY ','
        	    ENCLOSED BY '"'
        	    LINES TERMINATED BY '\\n'
        	    IGNORE 1 ROWS
        	    (time_id, user_id, start_time, end_time, @v_active) -- 1. CSV 파일에 적힌 순서대로 적으세요.
        	    SET active = CAST(TRIM(BOTH '\\r' FROM @v_active) AS UNSIGNED)
        	""";

            jdbc.execute(sql);
    	
    }
    
    public void loadLine() {
    	log.info("[Start Insert - Line.csv]");
        String sql = """
        		LOAD DATA INFILE '/var/lib/mysql-files/Line.csv'
        		INTO TABLE line
        		FIELDS TERMINATED BY ','
        		OPTIONALLY ENCLOSED BY '"'
        		LINES TERMINATED BY '\\n'
        		IGNORE 1 ROWS
        		(
        		    @v_line_id,
        		    user_id,
        		    plan_id,
        		    due_date_id,
        		    phone,
        		    status,
        		    start_date,
        		    @v_end_date,
        		    @v_is_representative
        		)
        		SET
        		    end_date = NULLIF(TRIM(BOTH '\\r' FROM @v_end_date), ''),
        		    is_representative = CAST(TRIM(BOTH '\\r' FROM @v_is_representative) AS UNSIGNED)
        		""";

            jdbc.execute(sql);
    }
    
    public void loadLineHistory() {
    	log.info("[Start Insert - LineHistory.csv]");
    	String sql = """
    			LOAD DATA INFILE '/var/lib/mysql-files/LineHistory.csv'
    			INTO TABLE line_history
    			FIELDS TERMINATED BY ','
    			OPTIONALLY ENCLOSED BY '"'
    			LINES TERMINATED BY '\\n'
    			IGNORE 1 ROWS
    			(
    			    @v_history_id,
    			    @v_line_id,
    			    plan_id,
    			    start_date,
    			    @v_end_date,
    			    user_id
    			)
    			SET
    			    end_date = NULLIF(TRIM(BOTH '\\r' FROM @v_end_date), '')
    			""";

                jdbc.execute(sql);
    }

    public void loadLineDiscount() {
    	log.info("[Start Insert - LineDisocunt.csv]");
    	String sql = """
    			LOAD DATA INFILE '/var/lib/mysql-files/LineDiscount.csv'
    			INTO TABLE line_discount
    			FIELDS TERMINATED BY ','
    			OPTIONALLY ENCLOSED BY '"'
    			LINES TERMINATED BY '\\n'
    			IGNORE 1 ROWS
    			(
    			    @v_discount_id,
    			    line_id,
    			    policy_id
    			)
    			""";

                jdbc.execute(sql);
    }

    public void loadLineVasSubscription() {
    	log.info("[Start Insert - LineVasSubscription.csv]");
    	String sql = """
    			LOAD DATA INFILE '/var/lib/mysql-files/LineVasSubscription.csv'
    			INTO TABLE line_vas_subscription
    			FIELDS TERMINATED BY ','
    			OPTIONALLY ENCLOSED BY '"'
    			LINES TERMINATED BY '\\n'
    			IGNORE 1 ROWS
    			(
    			    @v_sub_id,
    			    line_id,
    			    @v_vas_item_id,
    			    start_date,
    			    @v_end_date
    			)
    			SET
    			    end_date = NULLIF(TRIM(BOTH '\\r' FROM @v_end_date), '')
    			""";

                jdbc.execute(sql);
    }
    
    public void loadMicroPayment() {
    	log.info("[Start Insert - MicroPayment.csv]");
    	String sql = """
    		    LOAD DATA INFILE '/var/lib/mysql-files/MicroPayment.csv'
    		    INTO TABLE micropayment
    		    FIELDS TERMINATED BY ','
    		    OPTIONALLY ENCLOSED BY '"'
    		    LINES TERMINATED BY '\\n'
    		    IGNORE 1 ROWS
    		    (
    		        @v_micropayment_id,   -- auto_increment → 무시
    		        line_id,
    		        pay_month,
    		        pay_price
    		    )
    		""";

    		jdbc.execute(sql);
    }
    

    public void loadBillingHistory() {
    	log.info("[Start Insert - BillingHistory.csv]");
    	String sql = """
    			LOAD DATA INFILE '/var/lib/mysql-files/BillingHistory.csv'
    			INTO TABLE billing_history
    			FIELDS TERMINATED BY ','
    			OPTIONALLY ENCLOSED BY '"'
    			LINES TERMINATED BY '\\n'
    			IGNORE 1 ROWS
    			(
    			    @v_billing_id,
    			    line_id,
    			    plan_id,
    			    usage_amount,
    			    amount,
    			    user_at,
    			    billing_month,
    			    benefit_amount
    			)
    			""";

                jdbc.execute(sql);
    }
    
    public void loadUsageLog() {
    	log.info("[Start Insert - UsageLog.csv]");
    	String sql = """
    			LOAD DATA INFILE '/var/lib/mysql-files/UsageLog.csv'
    			INTO TABLE usage_log
    			FIELDS TERMINATED BY ','
    			OPTIONALLY ENCLOSED BY '"'
    			LINES TERMINATED BY '\\n'
    			IGNORE 1 ROWS
    			(
    			    @v_usage_log_id,
    			    item_type,
    			    used_amount,
    			    log_month,
    			    line_id
    			)
    			""";

                jdbc.execute(sql);
    }
    
    

    public void loadAll() {
    	log.info("[Start Insert All]");
    	loadMasterData();
        loadUser();
        loadBanTime();
        loadLine();
        loadLineHistory();
        loadLineDiscount();
        loadMicroPayment();
        loadLineVasSubscription();
        loadUsageLog();
        loadBillingHistory();
        log.info("[End Insert All]");
        
    }

    private void load(String path, String table) {
        String sql = """
            LOAD DATA INFILE '%s'
            INTO TABLE %s
            FIELDS TERMINATED BY ','
            ENCLOSED BY '"'
            LINES TERMINATED BY '\\n'
            IGNORE 1 ROWS
        """.formatted(path, table);

        jdbc.execute(sql);
    }
    
    
    private void executeSqlFile(String path) {
    	try {
            Resource resource = resourceLoader.getResource("classpath:" + path);

            String sql = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
            );
            
            String[] queries = sql.split(";");
            for (String query : queries) {
                if (!query.trim().isEmpty()) {
                	jdbc.execute(query.trim());
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException("마스터 SQL 실행 실패: " + path, e);
        }
    }

}
