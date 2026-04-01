package com.ozz.atlas.supply.supplier.controller;

import com.ozz.atlas.supply.supplier.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/supplier")
public class SupplierController {

    private final SupplierService supplierService;
    @Autowired
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

}
