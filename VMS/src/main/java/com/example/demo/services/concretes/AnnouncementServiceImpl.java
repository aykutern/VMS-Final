package com.example.demo.services.concretes;

import com.example.demo.dto.request.CreateAnnouncementRequest;
import com.example.demo.dto.request.UpdateAnnouncementRequest;
import com.example.demo.dto.response.AnnouncementResponse;
import com.example.demo.entities.concretes.Announcement;
import com.example.demo.entities.concretes.Project;
import com.example.demo.repositories.AnnouncementRepository;
import com.example.demo.repositories.ProjectRepository;
import com.example.demo.services.abstracts.AnnouncementService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ProjectRepository projectRepository;

    private static final int ACTIVE = 1;
    private static final int DELETED = 0;

    @Override
    public AnnouncementResponse create(CreateAnnouncementRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + request.getProjectId()));

        Announcement announcement = new Announcement();
        announcement.setProject(project);
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setIsActive(ACTIVE);

        return toResponse(announcementRepository.save(announcement));
    }

    @Override
    public AnnouncementResponse getById(Integer id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found: " + id));

        if (announcement.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Announcement not found: " + id);
        }

        return toResponse(announcement);
    }

    @Override
    public List<AnnouncementResponse> getAll(Integer projectId) {
        List<Announcement> list = (projectId == null)
                ? announcementRepository.findByIsActive(ACTIVE)
                : announcementRepository.findByProject_IdAndIsActive(projectId, ACTIVE);

        return list.stream().map(this::toResponse).toList();
    }

    @Override
    public AnnouncementResponse update(Integer id, UpdateAnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found: " + id));

        if (announcement.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("Announcement not found: " + id);
        }

        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());

        return toResponse(announcementRepository.save(announcement));
    }

    @Override
    public void delete(Integer id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found: " + id));

        if (announcement.getIsActive() == ACTIVE) {
            announcement.setIsActive(DELETED);
            announcementRepository.save(announcement);
        }
    }

    private AnnouncementResponse toResponse(Announcement announcement) {
        AnnouncementResponse response = new AnnouncementResponse();
        response.setId(announcement.getId());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());

        if (announcement.getProject() != null) {
            response.setProjectId(announcement.getProject().getId());
            response.setProjectName(announcement.getProject().getProjectName());
        }

        response.setCreatedAt(announcement.getCreatedAt());
        response.setUpdatedAt(announcement.getUpdatedAt());
        response.setCreatedBy(announcement.getCreatedBy());
        response.setUpdatedBy(announcement.getUpdatedBy());
        response.setIsActive(announcement.getIsActive());
        return response;
    }
}
