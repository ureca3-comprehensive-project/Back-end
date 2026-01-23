package org.backend.billing.bill.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ManualBillingRequest {
    private String targetDate;
}
