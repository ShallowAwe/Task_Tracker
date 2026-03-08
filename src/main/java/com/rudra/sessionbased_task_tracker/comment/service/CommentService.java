package com.rudra.sessionbased_task_tracker.comment.service;

import com.rudra.sessionbased_task_tracker.comment.dto.CreateCommentRequest;
import com.rudra.sessionbased_task_tracker.comment.dto.CommentResponse;
import com.rudra.sessionbased_task_tracker.comment.dto.UpdateCommentRequest;

import java.util.List;

public interface CommentService {

    CommentResponse addComment(Long projectId,
                               Long ticketId,
                               CreateCommentRequest request,
                               Long currentUserId);

    List<CommentResponse> getComments(Long projectId,
                                      Long ticketId,
                                      Long currentUserId);

    CommentResponse updateComment(Long projectId,
                                  Long ticketId,
                                  Long commentId,
                                  UpdateCommentRequest request,
                                  Long currentUserId);

    void deleteComment(Long projectId,
                       Long ticketId,
                       Long commentId,
                       Long currentUserId);
}