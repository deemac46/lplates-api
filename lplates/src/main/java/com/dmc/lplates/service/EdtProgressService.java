package com.dmc.lplates.service;

import java.util.List;

import com.dmc.lplates.inbound.models.EdtProgress;

public interface EdtProgressService {

    EdtProgress createEdtProgress(EdtProgress progress);
    EdtProgress getEdtProgressById(long id);
    List<EdtProgress> getEdtProgressByStudentId(long studentId);
    EdtProgress getEdtProgressByStudentAndModule(long studentId, int moduleNumber);
    EdtProgress markModuleCompleted(long studentId, int moduleNumber, long lessonId, String note, Long loggedByInstructorId);
    List<EdtProgress> getAllEdtProgress();
}
