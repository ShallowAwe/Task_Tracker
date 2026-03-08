package com.rudra.sessionbased_task_tracker.activity.repository;

import com.rudra.sessionbased_task_tracker.activity.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findTop20ByProjectIdOrderByCreatedAtDesc(Long projectId);
}