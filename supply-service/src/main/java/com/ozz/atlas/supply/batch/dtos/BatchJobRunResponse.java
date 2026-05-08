package com.ozz.atlas.supply.batch.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Batch Job Run 값 응답")
public record BatchJobRunResponse(
        String jobName,
        String runDate,
        Long executionId,
        String status
) {
}
