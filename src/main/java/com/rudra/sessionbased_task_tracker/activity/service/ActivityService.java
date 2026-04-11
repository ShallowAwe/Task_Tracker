package com.rudra.sessionbased_task_tracker.activity.service;

import com.rudra.sessionbased_task_tracker.activity.entity.Activity;
import com.rudra.sessionbased_task_tracker.activity.repository.ActivityRepository;
import com.rudra.sessionbased_task_tracker.project.entity.Project;
import com.rudra.sessionbased_task_tracker.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void log(Project project, User user, String action, String target, String toValue) {
        Activity activity = new Activity();
        activity.setProject(project);
        activity.setUser(user);
        activity.setAction(action);
        activity.setTarget(target);
        activity.setToValue(toValue);
        activity.setCreatedAt(LocalDateTime.now());
        activityRepository.save(activity);
    }
}
