package com.dmc.lplates.inbound.controllers;

import com.dmc.lplates.inbound.dtos.ApprovalStatusDto;
import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.inbound.models.Role;
import com.dmc.lplates.inbound.models.User;
import com.dmc.lplates.service.InstructorsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/instructors")
public class InstructorsController {

    private static final Set<String> VALID_APPROVAL_STATUSES = Set.of("pending", "approved", "rejected");
    private static final Map<String, String> IMAGE_EXTENSIONS_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    InstructorsServiceImpl instructorsService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public InstructorsController(InstructorsServiceImpl instructorsService) {
        this.instructorsService = instructorsService;
    }

    @GetMapping("/{instructorId}")
    public ResponseEntity<Instructor> getInstructorInfo(@PathVariable Long instructorId) {
        Instructor instructor = instructorsService.getInstructorById(instructorId);
        if (instructor != null) {
            return ResponseEntity.ok(instructor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<Instructor>> getInstructors() {
        List<Instructor> instructors = instructorsService.getAllInstructors();
        if (instructors != null) {
            return ResponseEntity.ok(instructors);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /** GET /instructors/pending — instructors awaiting approval. ADMIN only. */
    @GetMapping("/pending")
    public ResponseEntity<List<Instructor>> getPendingInstructors() {
        return ResponseEntity.ok(instructorsService.getPendingInstructors());
    }

    @PostMapping("/create")
    public ResponseEntity<String> createInstructor(@RequestBody Instructor instructor) {
        String result = instructorsService.createInstructor(instructor);
        if (result != null) {
            return ResponseEntity.ok("Instructor created successfully with ID: " + result);
        } else {
            return ResponseEntity.status(500).body("Failed to create Instructor");
        }
    }

    /** PATCH /instructors/{instructorId}/approval — approve/reject an instructor. ADMIN only. */
    @PatchMapping("/{instructorId}/approval")
    public ResponseEntity<?> updateApprovalStatus(@PathVariable Long instructorId,
                                                   @RequestBody ApprovalStatusDto dto) {
        String status = dto.getApprovalStatus() != null ? dto.getApprovalStatus().toLowerCase() : null;
        if (status == null || !VALID_APPROVAL_STATUSES.contains(status)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "approvalStatus must be one of: pending, approved, rejected"));
        }

        Instructor updated = instructorsService.updateApprovalStatus(instructorId, status);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /instructors/{instructorId}/profile-picture — upload a profile picture.
     * Only the instructor themselves or an ADMIN may update it.
     */
    @PostMapping(value = "/{instructorId}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfilePicture(@PathVariable Long instructorId,
                                                   @RequestParam("file") MultipartFile file,
                                                   Authentication authentication) {
        Instructor instructor = instructorsService.getInstructorById(instructorId);
        if (instructor == null) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = (User) authentication.getPrincipal();
        boolean isOwner = instructor.getUserId() != null && instructor.getUserId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You may only update your own instructor profile picture"));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        String extension = IMAGE_EXTENSIONS_BY_CONTENT_TYPE.get(file.getContentType());
        if (extension == null) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(Map.of("error", "Only JPEG, PNG, or WEBP images are allowed"));
        }

        try {
            Path instructorDir = Paths.get(uploadDir, "instructors", instructorId.toString()).toAbsolutePath().normalize();
            Files.createDirectories(instructorDir);

            // Filename is server-generated (UUID) - never derived from user input - to avoid path traversal.
            String filename = UUID.randomUUID() + extension;
            Path targetPath = instructorDir.resolve(filename).normalize();
            if (!targetPath.startsWith(instructorDir)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file path"));
            }

            file.transferTo(targetPath);

            String relativeUrl = "/uploads/instructors/" + instructorId + "/" + filename;
            Instructor updated = instructorsService.updateProfilePicture(instructorId, relativeUrl);
            return ResponseEntity.ok(updated);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to store the uploaded file"));
        }
    }

    @GetMapping("/{instructorId}/lessons")
    public ResponseEntity<Instructor> getLessonsForInstructor(@PathVariable Long instructorId) {
        Instructor instructor = instructorsService.getInstructorWithLessons(instructorId);
        if (instructor != null) {
            return ResponseEntity.ok(instructor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

