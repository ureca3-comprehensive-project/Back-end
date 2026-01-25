package org.backend.billing.bill.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.backend.billing.bill.dto.ManualBillingRequest;
import org.backend.port.BatchCommand;

import org.backend.port.BatchTriggerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BillingManualController {

    private final BatchTriggerPort batchTriggerPort;

    @PostMapping("/billing/runs")
    public ResponseEntity<String> runManualBilling(@RequestBody ManualBillingRequest req){

        String date = req.getTargetDate();
        String preDate = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        if(date == null || date.isBlank()){
            date = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        batchTriggerPort.trigger(new BatchCommand("billingJob", preDate));

        return ResponseEntity.ok("베치 실행 요청됨");
    }


}
