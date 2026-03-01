package com.example.demo.controllers;

import com.example.demo.dto.request.CreateVendorRequest;
import com.example.demo.dto.request.UpdateVendorRequest;
import com.example.demo.dto.response.VendorResponse;
import com.example.demo.services.abstracts.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @Operation(summary = "Create vendor")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendorResponse create(
            @Parameter(description = "Optional user header for auditing", example = "aykut.eren")
            @RequestHeader(value = "X-User", required = false) String xUser,
            @Valid @RequestBody CreateVendorRequest request
    ) {
        return vendorService.create(request);
    }

    @Operation(summary = "Get vendor by id")
    @GetMapping("/{id}")
    public VendorResponse getById(@PathVariable Integer id) {
        return vendorService.getById(id);
    }

    @Operation(summary = "List all vendors")
    @GetMapping
    public List<VendorResponse> getAll() {
        return vendorService.getAll();
    }

    @Operation(summary = "Update vendor")
    @PutMapping("/{id}")
    public VendorResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateVendorRequest request
    ) {
        return vendorService.update(id, request);
    }

    @Operation(summary = "Delete vendor")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        vendorService.delete(id);
    }
}
