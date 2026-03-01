package com.example.demo.services.concretes;

import com.example.demo.dto.request.CreateVendorRequest;
import com.example.demo.dto.request.UpdateVendorRequest;
import com.example.demo.dto.response.VendorResponse;
import com.example.demo.entities.concretes.Vendor;
import com.example.demo.repositories.VendorRepository;
import com.example.demo.services.abstracts.VendorService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;

    private static final int ACTIVE = 1;
    private static final int DELETED = 0;

    @Override
    public VendorResponse create(CreateVendorRequest request) {
        Vendor vendor = new Vendor();
        vendor.setVendorName(request.getVendorName());
        vendor.setIsActive(ACTIVE);

        return toResponse(vendorRepository.save(vendor));
    }

    @Override
    public VendorResponse getById(Integer id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found: " + id));

        if (vendor.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Vendor not found: " + id);
        }

        return toResponse(vendor);
    }

    @Override
    public List<VendorResponse> getAll() {
        return vendorRepository.findByIsActive(ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public VendorResponse update(Integer id, UpdateVendorRequest request) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found: " + id));

        if (vendor.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Vendor not found: " + id);
        }

        vendor.setVendorName(request.getVendorName());

        return toResponse(vendorRepository.save(vendor));
    }

    @Override
    public void delete(Integer id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found: " + id));

        if (vendor.getIsActive() == ACTIVE) {
            vendor.setIsActive(DELETED);
            vendorRepository.save(vendor);
        }
    }

    private VendorResponse toResponse(Vendor vendor) {
        VendorResponse response = new VendorResponse();
        response.setId(vendor.getId());
        response.setVendorName(vendor.getVendorName());
        response.setCreatedAt(vendor.getCreatedAt());
        response.setUpdatedAt(vendor.getUpdatedAt());
        response.setCreatedBy(vendor.getCreatedBy());
        response.setUpdatedBy(vendor.getUpdatedBy());
        response.setIsActive(vendor.getIsActive());
        return response;
    }
}
