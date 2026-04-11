package com.rudra.sessionbased_task_tracker.comment.controller;

import com.rudra.sessionbased_task_tracker.comment.dto.CommentResponse;
import com.rudra.sessionbased_task_tracker.comment.dto.CreateCommentRequest;
import com.rudra.sessionbased_task_tracker.comment.dto.UpdateCommentRequest;
import com.rudra.sessionbased_task_tracker.comment.service.CommentService;
import com.rudra.sessionbased_task_tracker.common.dto.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long projectId,
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal Long currentUserId) {

        CommentResponse response =
                commentService.addComment(projectId, ticketId, request, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long projectId,
            @PathVariable Long ticketId,
            @AuthenticationPrincipal Long currentUserId) {

        return ResponseEntity.ok(
                commentService.getComments(projectId, ticketId, currentUserId)
        );
    }


    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long projectId,
            @PathVariable Long ticketId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal Long currentUserId) {

        return ResponseEntity.ok(
                commentService.updateComment(projectId, ticketId, commentId, request, currentUserId)
        );
    }


    @DeleteMapping("/{commentId}")
    public ResponseEntity<MessageResponse> deleteComment(
            @PathVariable Long projectId,
            @PathVariable Long ticketId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long currentUserId) {

        commentService.deleteComment(projectId, ticketId, commentId, currentUserId);

        return ResponseEntity.ok(
                new MessageResponse("Comment deleted successfully")
        );
    }

}
