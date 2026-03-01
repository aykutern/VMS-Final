package com.example.demo.controllers;

import com.example.demo.dto.request.CreateProductManagerRequest;
import com.example.demo.dto.request.UpdateProductManagerRequest;
import com.example.demo.dto.response.ProductManagerResponse;
import com.example.demo.services.abstracts.ProductManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-managers")
@RequiredArgsConstructor
public class ProductManagerController {

    private final ProductManagerService productManagerService;

    @Operation(summary = "Create product manager")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductManagerResponse create(
            @Parameter(description = "Optional user header for auditing", example = "aykut.eren")
            @RequestHeader(value = "X-User", required = false) String xUser,
            @Valid @RequestBody CreateProductManagerRequest request
    ) {
        return productManagerService.create(request);
    }

    @Operation(summary = "Get product manager by id")
    @GetMapping("/{id}")
    public ProductManagerResponse getById(@PathVariable Integer id) {
        return productManagerService.getById(id);
    }

    @Operation(summary = "List all product managers")
    @GetMapping
    public List<ProductManagerResponse> getAll() {
        return productManagerService.getAll();
    }

    @Operation(summary = "Update product manager")
    @PutMapping("/{id}")
    public ProductManagerResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateProductManagerRequest request
    ) {
        return productManagerService.update(id, request);
    }

    @Operation(summary = "Delete product manager")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        productManagerService.delete(id);
    }
}
