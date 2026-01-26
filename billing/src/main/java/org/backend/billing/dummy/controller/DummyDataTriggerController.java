package org.backend.billing.dummy.controller;

import org.backend.billing.common.ApiResponse;
import org.backend.core.port.DummyDataTriggerPort;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/dummy-data")
@RequiredArgsConstructor
public class DummyDataTriggerController {
	
	private final DummyDataTriggerPort dummyDataJobPort;

    @PostMapping("/run")
    public ApiResponse<Long> runDummyData() {
        Long jobId = dummyDataJobPort.runDummyDataJob();
        return ApiResponse.ok(jobId);
    }

}
