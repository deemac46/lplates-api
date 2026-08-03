package com.dmc.lplates.database.repository;

import java.util.List;

import com.dmc.lplates.inbound.models.EdtProgress;

public interface EdtProgressRepository {

    EdtProgress insertEdtProgress(EdtProgress progress);
    EdtProgress getEdtProgressById(long id);
    List<EdtProgress> getEdtProgressByStudentId(long studentId);
    EdtProgress getEdtProgressByStudentAndModule(long studentId, int moduleNumber);
    EdtProgress markModuleCompleted(long studentId, int moduleNumber, long lessonId);
    List<EdtProgress> getAllEdtProgress();
}
