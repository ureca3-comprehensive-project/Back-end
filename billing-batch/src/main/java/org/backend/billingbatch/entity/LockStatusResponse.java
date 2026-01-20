package org.backend.billingbatch.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LockStatusResponse {
    private boolean locked;
    private int runningCount;
    private String statusMessage;
}
