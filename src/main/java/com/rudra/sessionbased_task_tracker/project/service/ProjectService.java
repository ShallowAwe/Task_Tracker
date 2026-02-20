package com.rudra.sessionbased_task_tracker.project.service;

import com.rudra.sessionbased_task_tracker.project.dto.CreateProjectRequest;
import com.rudra.sessionbased_task_tracker.project.dto.UpdateProjectRequest;
import com.rudra.sessionbased_task_tracker.project.dto.ProjectResponse;
import com.rudra.sessionbased_task_tracker.project.entity.Project;
import com.rudra.sessionbased_task_tracker.project.exception.ProjectAlreadyExistsException;
import com.rudra.sessionbased_task_tracker.project.exception.ProjectNotFoundException;
import com.rudra.sessionbased_task_tracker.project.repository.ProjectRepository;
import com.rudra.sessionbased_task_tracker.user.entity.User;
import com.rudra.sessionbased_task_tracker.user.exception.UserNotFoundException;
import com.rudra.sessionbased_task_tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectResponse createProject(CreateProjectRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (projectRepository.existsByKey(request.getKey())) {
            throw new ProjectAlreadyExistsException(request.getKey());
        }

        LocalDateTime now = LocalDateTime.now();

        Project project = new Project();
        project.setKey(request.getKey());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(user);
        project.setArchivedFlag(false);
        project.setCreatedAt(now);
        project.setUpdatedAt(now);

        Project saved = projectRepository.save(project);
        return mapToResponse(saved);
    }

    public ProjectResponse getProjectById(Long id, Long userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        if (!project.getOwner().getId().equals(userId)) {
            throw new ProjectNotFoundException("Project not found with id: " + id);
        }

        return mapToResponse(project);
    }

    public ProjectResponse getProjectByKey(String key, Long userId) {
        Project project = projectRepository.findByKey(key)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with key: " + key));

        if (!project.getOwner().getId().equals(userId)) {
            throw new ProjectNotFoundException("Project not found with key: " + key);
        }

        return mapToResponse(project);
    }

    public List<ProjectResponse> getAllProjectsByUser(Long userId) {
        return projectRepository.findByOwnerId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> getActiveProjectsByUser(Long userId) {
        return projectRepository.findByOwnerIdAndArchivedFlagFalse(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse updateProject(Long id, UpdateProjectRequest request, Long userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        if (!project.getOwner().getId().equals(userId)) {
            throw new ProjectNotFoundException("Project not found with id: " + id);
        }

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        project.setUpdatedAt(LocalDateTime.now());

        Project updated = projectRepository.save(project);
        return mapToResponse(updated);
    }

    public void archiveProject(Long id, Long userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        if (!project.getOwner().getId().equals(userId)) {
            throw new ProjectNotFoundException("Project not found with id: " + id);
        }

        project.setArchivedFlag(true);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    public void unarchiveProject(Long id, Long userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        if (!project.getOwner().getId().equals(userId)) {
            throw new ProjectNotFoundException("Project not found with id: " + id);
        }

        project.setArchivedFlag(false);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    public void deleteProject(Long id, Long userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        if (!project.getOwner().getId().equals(userId)) {
            throw new ProjectNotFoundException("Project not found with id: " + id);
        }

        projectRepository.delete(project);
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .key(project.getKey())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwner().getId())
                .archived(project.isArchivedFlag())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}