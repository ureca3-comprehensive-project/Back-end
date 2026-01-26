package org.backend.billingbatch.dummy;

import org.backend.core.port.DummyDataTriggerPort;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DummyDataTriggerAdapter implements DummyDataTriggerPort{

	private final CsvLoadService service;

    @Override
    public Long runDummyDataJob() {
    	try {    		
    		service.loadAll();;
    	} catch (Exception e) {
    		e.printStackTrace();
    		return 0L;
    	}
    	
    	return 1L;
    }
}
