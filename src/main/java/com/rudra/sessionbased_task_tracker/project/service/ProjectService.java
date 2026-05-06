package com.rudra.sessionbased_task_tracker.project.service;

import com.rudra.sessionbased_task_tracker.activity.service.ActivityService;
import com.rudra.sessionbased_task_tracker.project.dto.CreateProjectRequest;
import com.rudra.sessionbased_task_tracker.project.dto.UpdateProjectRequest;
import com.rudra.sessionbased_task_tracker.project.dto.ProjectResponse;
import com.rudra.sessionbased_task_tracker.project.entity.Project;
import com.rudra.sessionbased_task_tracker.project.exception.ProjectAlreadyExistsException;
import com.rudra.sessionbased_task_tracker.project.exception.ProjectNotFoundException;
import com.rudra.sessionbased_task_tracker.project.repository.ProjectRepository;
import com.rudra.sessionbased_task_tracker.projectMember.exception.InsufficientPermissionException;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectMember;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectRole;
import com.rudra.sessionbased_task_tracker.projectMember.repository.ProjectMemberRepository;
import com.rudra.sessionbased_task_tracker.user.entity.User;
import com.rudra.sessionbased_task_tracker.user.exception.UserNotFoundException;
import com.rudra.sessionbased_task_tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectAccessService projectAccessService;
    private final ActivityService activityService;

    static final String projectNotFoundExceptionString = "Project not found with id: ";

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(projectNotFoundExceptionString + userId));

        if (projectRepository.existsByKeyAndDeletedFalse(request.getKey())) {
            throw new ProjectAlreadyExistsException(request.getKey());
        }

        Project project = new Project();
        project.setKey(request.getKey());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(user);
        project.setArchivedFlag(false);

        Project saved = projectRepository.save(project);

        // Auto-assign OWNER membership
        ProjectMember ownerMembership = new ProjectMember();
        ownerMembership.setProject(saved);
        ownerMembership.setUser(user);
        ownerMembership.setRole(ProjectRole.OWNER);
        ownerMembership.setLastAccessedAt(LocalDateTime.now());
        projectMemberRepository.save(ownerMembership);

        activityService.log(saved, user, "created", "Project", saved.getName());

        return mapToResponse(saved);
    }

    public ProjectResponse getProjectById(Long id, Long userId) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        // Any project member can view the project
        if (!projectMemberRepository.existsByProjectIdAndUserId(id, userId)) {
            throw new ProjectNotFoundException("Project not found with id: " + id);
        }

        return mapToResponse(project);
    }

    public ProjectResponse getProjectByKey(String key, Long userId) {
        Project project = projectRepository.findByKeyAndDeletedFalse(key)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with key: " + key));

        if (!projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId)) {
            throw new ProjectNotFoundException("Project not found with key: " + key);
        }

        return mapToResponse(project);
    }

    public List<ProjectResponse> getAllProjectsByUser(Long userId) {
        return projectAccessService.getActiveMemberships(userId).stream()
                .map(ProjectMember::getProject)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> getActiveProjectsByUser(Long userId) {
        return projectAccessService.getActiveMemberships(userId).stream()
                .map(ProjectMember::getProject)
                .filter(project -> !project.isArchivedFlag())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void selectProject(String projectKey, Long userId) {
        Project project = projectRepository.findByKeyAndDeletedFalse(projectKey)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with key: " + projectKey));

        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(project.getId(), userId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with key: " + projectKey));

        membership.setLastAccessedAt(LocalDateTime.now());
        projectMemberRepository.save(membership);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request, Long userId) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        // OWNER or MAINTAINER can update project details
        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(id, userId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        if (!membership.getRole().isAtLeast(ProjectRole.MAINTAINER)) {
            throw new InsufficientPermissionException("Only OWNER or MAINTAINER can update project details");
        }

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        return mapToResponse(project);
    }

    @Transactional
    public void archiveProject(Long id, Long userId) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        if (!project.getOwner().getId().equals(userId)) {
            throw new InsufficientPermissionException("Only OWNER can archive a project");
        }

        project.setArchivedFlag(true);

        activityService.log(project, project.getOwner(), "archived", "Project", project.getName());
    }

    @Transactional
    public void unarchiveProject(Long id, Long userId) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        if (!project.getOwner().getId().equals(userId)) {
            throw new InsufficientPermissionException("Only OWNER can unarchive a project");
        }

        project.setArchivedFlag(false);

        activityService.log(project, project.getOwner(), "unarchived", "Project", project.getName());
    }

    @Transactional
    public void deleteProject(Long id, Long userId) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

      if (!project.getOwner().getId().equals(userId)){
          throw new InsufficientPermissionException("Only owner can delete the project");
      }

        project.setDeleted(true);
        project.setDeletedAt(LocalDateTime.now());
        project.setDeletedBy(project.getOwner());

        activityService.log(project, project.getOwner(), "deleted", "Project", project.getName());
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
