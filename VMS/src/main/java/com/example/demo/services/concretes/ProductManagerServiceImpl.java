package com.example.demo.services.concretes;

import com.example.demo.dto.request.CreateProductManagerRequest;
import com.example.demo.dto.request.UpdateProductManagerRequest;
import com.example.demo.dto.response.ProductManagerResponse;
import com.example.demo.entities.concretes.ProductManager;
import com.example.demo.repositories.ProductManagerRepository;
import com.example.demo.services.abstracts.ProductManagerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductManagerServiceImpl implements ProductManagerService {

    private final ProductManagerRepository productManagerRepository;

    private static final int ACTIVE = 1;
    private static final int DELETED = 0;

    @Override
    public ProductManagerResponse create(CreateProductManagerRequest request) {
        ProductManager pm = new ProductManager();
        pm.setName(request.getName());
        pm.setTitle(request.getTitle());
        pm.setIsActive(ACTIVE);

        return toResponse(productManagerRepository.save(pm));
    }

    @Override
    public ProductManagerResponse getById(Integer id) {
        ProductManager pm = productManagerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductManager not found: " + id));

        if (pm.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("ProductManager not found: " + id);
        }

        return toResponse(pm);
    }

    @Override
    public List<ProductManagerResponse> getAll() {
        return productManagerRepository.findByIsActive(ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProductManagerResponse update(Integer id, UpdateProductManagerRequest request) {
        ProductManager pm = productManagerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductManager not found: " + id));

        if (pm.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("ProductManager not found: " + id);
        }

        pm.setName(request.getName());
        pm.setTitle(request.getTitle());

        return toResponse(productManagerRepository.save(pm));
    }

    @Override
    public void delete(Integer id) {
        ProductManager pm = productManagerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductManager not found: " + id));

        if (pm.getIsActive() == ACTIVE) {
            pm.setIsActive(DELETED);
            productManagerRepository.save(pm);
        }
    }

    private ProductManagerResponse toResponse(ProductManager pm) {
        ProductManagerResponse response = new ProductManagerResponse();
        response.setId(pm.getId());
        response.setName(pm.getName());
        response.setTitle(pm.getTitle());
        response.setCreatedAt(pm.getCreatedAt());
        response.setUpdatedAt(pm.getUpdatedAt());
        response.setCreatedBy(pm.getCreatedBy());
        response.setUpdatedBy(pm.getUpdatedBy());
        response.setIsActive(pm.getIsActive());
        return response;
    }
}
