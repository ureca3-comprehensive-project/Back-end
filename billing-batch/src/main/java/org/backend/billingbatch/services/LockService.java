package org.backend.billingbatch.services;

import lombok.RequiredArgsConstructor;
import org.backend.domain.config.LockStatusResponse;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class LockService {
    private final JobExplorer jobExplorer;
    private final AtomicBoolean isBatchRunning = new AtomicBoolean(false);

    // 락 획득 시도 메서드 (1명만 true 반환) - api 명세서에는 없는데 필요하다 판단하여 추가
    public boolean tryAcquireLock() {
        // 현재 값이 false이면 true로 바꾸고 성공(true) 반환
        // 이미 true라면 실패(false) 반환
        return isBatchRunning.compareAndSet(false, true);
    }
    public void forceUnlock() {
        isBatchRunning.set(false);
    }

    public LockStatusResponse getLockStatus() {
        // 현재 실행중인 Job이 있는지 확인
        Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("createInvoiceJob");
        boolean isLocked = !runningExecutions.isEmpty();
        int count = runningExecutions.size();

        return LockStatusResponse.builder()
                .locked(isLocked)
                .runningCount(count)
                .statusMessage(isLocked ? "BATCH_RUNNING" : "IDLE") // 상태 메시지 동적으로 생성
                .build();
    }
}
