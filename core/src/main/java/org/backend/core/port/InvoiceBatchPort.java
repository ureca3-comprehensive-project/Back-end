package org.backend.core.port;

import org.backend.core.dto.BatchRunRequest;
import org.backend.core.dto.BatchRunResponse;
import org.backend.core.dto.LockStatusResponse;

import java.util.List;
import java.util.Map;

public interface InvoiceBatchPort {
    // 1. 배치 요약 및 에러
    Map<String, Object> getBatchSummary(String jobName);
    List<String> getJobErrors(Long runId);

    // 2. 배치 제어
    Long retryJob(Long runId);
    void stopJob(Long runId);

    // 3. 배치 조회
    List<BatchRunResponse> getJobExecutions(int offset, int limit);
    BatchRunResponse getJobExecutionDetail(Long runId);

    // 4. 배치 실행
    Long runJob(BatchRunRequest request);

    // 5. 수동 실행 (락 로직 포함)
    Long runJobManually(BatchRunRequest request);

    // 6. 리포트
    Map<String, Object> getDuplicateReport(String billingMonth);

    // 락 상태 조회 기능
    LockStatusResponse getLockStatus();

    // 락 강제 해제
    void forceUnlock();

    // 락 획득
//    public boolean tryAcquireLock();
}
