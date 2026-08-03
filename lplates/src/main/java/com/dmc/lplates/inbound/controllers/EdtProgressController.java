package com.dmc.lplates.inbound.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmc.lplates.inbound.models.EdtProgress;
import com.dmc.lplates.service.EdtProgressService;

@RestController
@RequestMapping("/edt")
public class EdtProgressController {

    private final EdtProgressService edtProgressService;

    public EdtProgressController(EdtProgressService edtProgressService) {
        this.edtProgressService = edtProgressService;
    }

    @PostMapping("/create")
    public ResponseEntity<EdtProgress> createEdtProgress(@RequestBody EdtProgress progress) {
        EdtProgress created = edtProgressService.createEdtProgress(progress);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EdtProgress> getEdtProgressById(@PathVariable long id) {
        EdtProgress progress = edtProgressService.getEdtProgressById(id);
        if (progress == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EdtProgress>> getEdtProgressByStudentId(@PathVariable long studentId) {
        return ResponseEntity.ok(edtProgressService.getEdtProgressByStudentId(studentId));
    }

    @GetMapping("/student/{studentId}/module/{moduleNumber}")
    public ResponseEntity<EdtProgress> getEdtProgressByStudentAndModule(
            @PathVariable long studentId, @PathVariable int moduleNumber) {
        EdtProgress progress = edtProgressService.getEdtProgressByStudentAndModule(studentId, moduleNumber);
        if (progress == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(progress);
    }

    @PutMapping("/complete/{studentId}/{moduleNumber}/{lessonId}")
    public ResponseEntity<EdtProgress> markModuleCompleted(
            @PathVariable long studentId,
            @PathVariable int moduleNumber,
            @PathVariable long lessonId) {
        EdtProgress progress = edtProgressService.markModuleCompleted(studentId, moduleNumber, lessonId);
        if (progress == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/")
    public ResponseEntity<List<EdtProgress>> getAllEdtProgress() {
        return ResponseEntity.ok(edtProgressService.getAllEdtProgress());
    }
}
