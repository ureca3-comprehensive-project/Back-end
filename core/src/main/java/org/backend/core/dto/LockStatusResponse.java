package org.backend.core.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LockStatusResponse {
    private boolean locked;
    private int runningCount;
    private String statusMessage;
}
