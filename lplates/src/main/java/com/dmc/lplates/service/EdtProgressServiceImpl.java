package com.dmc.lplates.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dmc.lplates.database.repository.EdtProgressRepository;
import com.dmc.lplates.inbound.models.EdtProgress;

@Service
public class EdtProgressServiceImpl implements EdtProgressService {

    private final EdtProgressRepository edtProgressRepository;

    public EdtProgressServiceImpl(EdtProgressRepository edtProgressRepository) {
        this.edtProgressRepository = edtProgressRepository;
    }

    @Override
    public EdtProgress createEdtProgress(EdtProgress progress) {
        return edtProgressRepository.insertEdtProgress(progress);
    }

    @Override
    public EdtProgress getEdtProgressById(long id) {
        return edtProgressRepository.getEdtProgressById(id);
    }

    @Override
    public List<EdtProgress> getEdtProgressByStudentId(long studentId) {
        return edtProgressRepository.getEdtProgressByStudentId(studentId);
    }

    @Override
    public EdtProgress getEdtProgressByStudentAndModule(long studentId, int moduleNumber) {
        return edtProgressRepository.getEdtProgressByStudentAndModule(studentId, moduleNumber);
    }

    @Override
    public EdtProgress markModuleCompleted(long studentId, int moduleNumber, long lessonId) {
        return edtProgressRepository.markModuleCompleted(studentId, moduleNumber, lessonId);
    }

    @Override
    public List<EdtProgress> getAllEdtProgress() {
        return edtProgressRepository.getAllEdtProgress();
    }
}
