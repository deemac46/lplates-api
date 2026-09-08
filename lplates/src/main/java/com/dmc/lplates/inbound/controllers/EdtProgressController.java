package com.dmc.lplates.inbound.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmc.lplates.inbound.models.EdtProgress;
import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.User;
import com.dmc.lplates.service.EdtProgressService;
import com.dmc.lplates.service.BusinessValidationService;
import com.dmc.lplates.service.ResourceAuthorizationService;

@RestController
@RequestMapping("/edt")
public class EdtProgressController {

    private final EdtProgressService edtProgressService;
    private final ResourceAuthorizationService authorizationService;
    private final BusinessValidationService validationService;

    public EdtProgressController(EdtProgressService edtProgressService,
                                 ResourceAuthorizationService authorizationService,
                                 BusinessValidationService validationService) {
        this.edtProgressService = edtProgressService;
        this.authorizationService = authorizationService;
        this.validationService = validationService;
    }

    @PostMapping("/create")
    public ResponseEntity<EdtProgress> createEdtProgress(@RequestBody EdtProgress progress,
                                                         Authentication authentication) {
        User currentUser = authorizationService.currentUser(authentication);
        if (progress.getLessonId() == null) {
            throw new IllegalArgumentException("lessonId is required");
        }
        Booking lesson = authorizationService.requireAssignedInstructor(progress.getLessonId(), currentUser);
        progress.setStudentId(lesson.getStudentId());
        validationService.validateEdtProgress(progress, lesson);
        EdtProgress created = edtProgressService.createEdtProgress(progress);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EdtProgress> getEdtProgressById(@PathVariable long id,
                                                           Authentication authentication) {
        EdtProgress progress = edtProgressService.getEdtProgressById(id);
        if (progress == null) return ResponseEntity.notFound().build();
        authorizationService.requireStudent(progress.getStudentId(), authorizationService.currentUser(authentication));
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EdtProgress>> getEdtProgressByStudentId(@PathVariable long studentId,
                                                                       Authentication authentication) {
        authorizationService.requireStudent(studentId, authorizationService.currentUser(authentication));
        return ResponseEntity.ok(edtProgressService.getEdtProgressByStudentId(studentId));
    }

    @GetMapping("/student/{studentId}/module/{moduleNumber}")
    public ResponseEntity<EdtProgress> getEdtProgressByStudentAndModule(
            @PathVariable long studentId, @PathVariable int moduleNumber, Authentication authentication) {
        authorizationService.requireStudent(studentId, authorizationService.currentUser(authentication));
        EdtProgress progress = edtProgressService.getEdtProgressByStudentAndModule(studentId, moduleNumber);
        if (progress == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(progress);
    }

    @PutMapping("/complete/{studentId}/{moduleNumber}/{lessonId}")
    public ResponseEntity<EdtProgress> markModuleCompleted(
            @PathVariable long studentId,
            @PathVariable int moduleNumber,
            @PathVariable long lessonId,
            Authentication authentication) {
        User currentUser = authorizationService.currentUser(authentication);
        Booking lesson = authorizationService.requireAssignedInstructor(lessonId, currentUser);
        if (!lesson.getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("studentId does not match the lesson learner");
        }
        EdtProgress requestedProgress = new EdtProgress();
        requestedProgress.setModuleNumber(moduleNumber);
        validationService.validateEdtProgress(requestedProgress, lesson);
        EdtProgress progress = edtProgressService.markModuleCompleted(
            studentId, moduleNumber, lessonId, null, lesson.getInstructorId());
        if (progress == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/")
    public ResponseEntity<List<EdtProgress>> getAllEdtProgress(Authentication authentication) {
        authorizationService.requireAdmin(authorizationService.currentUser(authentication));
        return ResponseEntity.ok(edtProgressService.getAllEdtProgress());
    }
}
