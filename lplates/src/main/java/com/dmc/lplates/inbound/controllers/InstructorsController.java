package com.dmc.lplates.inbound.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dmc.lplates.inbound.dtos.ApprovalStatusDto;
import com.dmc.lplates.inbound.dtos.AvailabilityDto;
import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.inbound.models.Role;
import com.dmc.lplates.inbound.models.User;
import com.dmc.lplates.service.BusinessValidationService;
import com.dmc.lplates.service.InstructorsServiceImpl;
import com.dmc.lplates.service.ResourceAuthorizationService;

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
    ResourceAuthorizationService authorizationService;
    BusinessValidationService validationService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public InstructorsController(InstructorsServiceImpl instructorsService,
                                 ResourceAuthorizationService authorizationService,
                                 BusinessValidationService validationService) {
        this.instructorsService = instructorsService;
        this.authorizationService = authorizationService;
        this.validationService = validationService;
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
        List<Instructor> instructors = instructorsService.getAvailableInstructors();
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
    public ResponseEntity<Instructor> createInstructor(@RequestBody Instructor instructor,
                                                        Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        instructor.setUserId(currentUser.getId());
        validationService.validateNewInstructor(currentUser.getId());
        Instructor created = instructorsService.createInstructor(instructor);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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

    /** PATCH /instructors/{instructorId}/availability — quickly toggle booking availability. Owning INSTRUCTOR or ADMIN. */
    @PatchMapping("/{instructorId}/availability")
    public ResponseEntity<?> updateAvailability(@PathVariable Long instructorId,
                                                 @RequestBody AvailabilityDto dto,
                                                 Authentication authentication) {
        if (dto.getAvailable() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "available is required"));
        }
        authorizationService.requireOwnInstructor(instructorId, authorizationService.currentUser(authentication));
        Instructor updated = instructorsService.updateAvailability(instructorId, dto.getAvailable());
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
    public ResponseEntity<Instructor> getLessonsForInstructor(@PathVariable Long instructorId,
                                                               Authentication authentication) {
        authorizationService.requireOwnInstructor(instructorId, authorizationService.currentUser(authentication));
        Instructor instructor = instructorsService.getInstructorWithLessons(instructorId);
        if (instructor != null) {
            return ResponseEntity.ok(instructor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

