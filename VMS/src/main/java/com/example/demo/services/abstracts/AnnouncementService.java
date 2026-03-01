package com.example.demo.services.abstracts;

import com.example.demo.dto.request.CreateAnnouncementRequest;
import com.example.demo.dto.request.UpdateAnnouncementRequest;
import com.example.demo.dto.response.AnnouncementResponse;

import java.util.List;

public interface AnnouncementService {
    AnnouncementResponse create(CreateAnnouncementRequest request);
    AnnouncementResponse getById(Integer id);
    List<AnnouncementResponse> getAll(Integer projectId);
    AnnouncementResponse update(Integer id, UpdateAnnouncementRequest request);
    void delete(Integer id);
}
