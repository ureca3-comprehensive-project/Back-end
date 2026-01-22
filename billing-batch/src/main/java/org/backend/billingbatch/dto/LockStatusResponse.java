package org.backend.billingbatch.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LockStatusResponse {
    private boolean locked;
    private int runningCount;
    private String statusMessage;
}
