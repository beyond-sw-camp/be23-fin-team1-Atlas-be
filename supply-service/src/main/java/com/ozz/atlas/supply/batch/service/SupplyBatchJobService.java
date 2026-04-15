package com.ozz.atlas.supply.batch.service;

import com.ozz.atlas.supply.batch.dtos.BatchJobRunResponse;
import com.ozz.atlas.supply.batch.exception.BatchErrorCode;
import com.ozz.atlas.supply.batch.exception.BatchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class SupplyBatchJobService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final JobLauncher jobLauncher;
    private final Job certificateExpiryWarningJob;
    private final Job lotExpiryAggregationJob;
    private final Job supplierDeliveryKpiJob;

    // 한 번에 하나의 배치만 실행되게 막는 락
    private final ReentrantLock batchExecutionLock = new ReentrantLock(true);

    public SupplyBatchJobService(
            JobLauncher jobLauncher,
            @Qualifier("certificateExpiryWarningJob") Job certificateExpiryWarningJob,
            @Qualifier("lotExpiryAggregationJob") Job lotExpiryAggregationJob,
            @Qualifier("supplierDeliveryKpiJob") Job supplierDeliveryKpiJob
    ) {
        this.jobLauncher = jobLauncher;
        this.certificateExpiryWarningJob = certificateExpiryWarningJob;
        this.lotExpiryAggregationJob = lotExpiryAggregationJob;
        this.supplierDeliveryKpiJob = supplierDeliveryKpiJob;
    }

    public BatchJobRunResponse runCertificateExpiryWarning(LocalDate runDate) {
        LocalDate targetDate = runDate != null ? runDate : LocalDate.now(KST);
        return runSingle(certificateExpiryWarningJob, targetDate);
    }

    public BatchJobRunResponse runLotExpiryAggregation(LocalDate runDate) {
        LocalDate targetDate = runDate != null ? runDate : LocalDate.now(KST);
        return runSingle(lotExpiryAggregationJob, targetDate);
    }

    public BatchJobRunResponse runSupplierDeliveryKpi(LocalDate runDate) {
        LocalDate targetDate = runDate != null ? runDate : LocalDate.now(KST).minusDays(1);
        return runSingle(supplierDeliveryKpiJob, targetDate);
    }

    public List<BatchJobRunResponse> runDaily(LocalDate baseDate) {
        acquireBatchLock();

        try {
            LocalDate targetBaseDate = baseDate != null ? baseDate : LocalDate.now(KST);

            List<BatchJobRunResponse> results = new ArrayList<>();
            results.add(runInternal(certificateExpiryWarningJob, targetBaseDate));
            results.add(runInternal(lotExpiryAggregationJob, targetBaseDate));
            results.add(runInternal(supplierDeliveryKpiJob, targetBaseDate.minusDays(1)));
            return results;
        } finally {
            batchExecutionLock.unlock();
        }
    }

    private BatchJobRunResponse runSingle(Job job, LocalDate runDate) {
        acquireBatchLock();

        try {
            return runInternal(job, runDate);
        } finally {
            batchExecutionLock.unlock();
        }
    }

    private void acquireBatchLock() {
        if (!batchExecutionLock.tryLock()) {
            throw new BatchException(BatchErrorCode.BATCH_ALREADY_RUNNING);
        }
    }

    private BatchJobRunResponse runInternal(Job job, LocalDate runDate) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("runDate", runDate.toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            return new BatchJobRunResponse(
                    job.getName(),
                    runDate.toString(),
                    execution.getId(),
                    execution.getStatus().name()
            );
        } catch (Exception e) {
            log.error("Batch run failed. jobName={}, runDate={}", job.getName(), runDate, e);
            throw new BatchException(BatchErrorCode.BATCH_EXECUTION_FAILED);
        }
    }
}
