package com.example.demo.services.abstracts;

import com.example.demo.dto.request.CreateVendorRequest;
import com.example.demo.dto.request.UpdateVendorRequest;
import com.example.demo.dto.response.VendorResponse;

import java.util.List;

public interface VendorService {
    VendorResponse create(CreateVendorRequest request);
    VendorResponse getById(Integer id);
    List<VendorResponse> getAll();
    VendorResponse update(Integer id, UpdateVendorRequest request);
    void delete(Integer id);
}
