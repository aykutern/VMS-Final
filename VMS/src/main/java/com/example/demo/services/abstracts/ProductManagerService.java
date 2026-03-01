package com.example.demo.services.abstracts;

import com.example.demo.dto.request.CreateProductManagerRequest;
import com.example.demo.dto.request.UpdateProductManagerRequest;
import com.example.demo.dto.response.ProductManagerResponse;

import java.util.List;

public interface ProductManagerService {
    ProductManagerResponse create(CreateProductManagerRequest request);
    ProductManagerResponse getById(Integer id);
    List<ProductManagerResponse> getAll();
    ProductManagerResponse update(Integer id, UpdateProductManagerRequest request);
    void delete(Integer id);
}
