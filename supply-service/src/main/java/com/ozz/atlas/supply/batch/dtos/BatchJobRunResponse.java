package com.ozz.atlas.supply.batch.dtos;

public record BatchJobRunResponse(
        String jobName,
        String runDate,
        Long executionId,
        String status
) {
}
