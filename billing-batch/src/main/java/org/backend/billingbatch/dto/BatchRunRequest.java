package org.backend.billingbatch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BatchRunRequest {
    private String jobName;       // 실행할 job 이름 - 확인을 위해
    private String billingMonth;  // 파라미터 (ex: 2024-01)
    private boolean isForced;    // 강제 실행 여부 (락 무시 등)
}
