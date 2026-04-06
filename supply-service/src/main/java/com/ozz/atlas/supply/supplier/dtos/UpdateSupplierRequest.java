package com.ozz.atlas.supply.supplier.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSupplierRequest {

    @NotBlank
    @Size(max = 50)
    private String supplierCode;

    @NotBlank
    @Size(max = 100)
    private String supplierName;

    @NotNull
    @Min(1)
    private Integer tierLevel;

    @Size(max = 50)
    private String primaryContactName;

    @Email
    @Size(max = 100)
    private String primaryContactEmail;

    @Size(max = 30)
    private String primaryContactPhone;
}
