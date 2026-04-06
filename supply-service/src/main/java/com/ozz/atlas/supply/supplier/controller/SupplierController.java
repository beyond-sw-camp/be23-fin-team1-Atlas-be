package com.ozz.atlas.supply.supplier.controller;

import com.ozz.atlas.supply.supplier.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/supplier")
public class SupplierController {

    private final SupplierService supplierService;

}
