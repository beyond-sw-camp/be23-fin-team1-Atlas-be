package com.ozz.atlas.auth.dtos;

import com.ozz.atlas.auth.domain.OrganizationType;
import com.ozz.atlas.common.jpa.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationSearchDto {
    private OrganizationType organizationType;
    private String organizationName;
    private Status status;
    private String keyword;

}
