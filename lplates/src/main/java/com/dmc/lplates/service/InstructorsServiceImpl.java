package com.dmc.lplates.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dmc.lplates.database.repository.InstructorRepository;
import com.dmc.lplates.inbound.models.Instructor;

@Service
public class InstructorsServiceImpl implements InstructorsService {

    private final InstructorRepository instructorRepository;

    public InstructorsServiceImpl(@Autowired InstructorRepository instructorRepository) {
        this.instructorRepository = instructorRepository;
    }

    @Override
    public List<Instructor> getAllInstructors() {
        return instructorRepository.getAllInstructors();
    }

    @Override
    public List<Instructor> getAvailableInstructors() {
        return instructorRepository.getAvailableInstructors();
    }

    @Override
    public Instructor getInstructorById(Long instructorId) {
        return instructorRepository.getInstructorById(instructorId);
    }

    @Override
    public Instructor createInstructor(Instructor instructor) {
        Long instructorId = instructorRepository.createInstructor(instructor);
        if (instructorId == null) {
            throw new IllegalStateException("Failed to create instructor");
        }
        Instructor created = instructorRepository.getInstructorById(instructorId);
        if (created == null) {
            throw new IllegalStateException("Instructor created but could not be loaded");
        }
        return created;
    }

    @Override
    public Instructor getInstructorWithLessons(Long instructorId) {
        return instructorRepository.getInstructorWithLessons(instructorId);
    }

    @Override
    public Instructor getInstructorByUserId(Long userId) {
        return instructorRepository.getInstructorByUserId(userId);
    }

    @Override
    public List<Instructor> getPendingInstructors() {
        return instructorRepository.getPendingInstructors();
    }

    @Override
    public Instructor updateApprovalStatus(Long instructorId, String approvalStatus) {
        return instructorRepository.updateApprovalStatus(instructorId, approvalStatus);
    }

    @Override
    public Instructor updateAvailability(Long instructorId, boolean available) {
        return instructorRepository.updateAvailability(instructorId, available);
    }

    @Override
    public Instructor updateProfilePicture(Long instructorId, String profilePicture) {
        return instructorRepository.updateProfilePicture(instructorId, profilePicture);
    }
}
