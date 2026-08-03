package com.dmc.lplates.service;

import com.dmc.lplates.database.repository.InstructorRepository;
import com.dmc.lplates.inbound.models.Instructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public Instructor getInstructorById(Long instructorId) {
        return instructorRepository.getInstructorById(instructorId);
    }

    @Override
    public String createInstructor(Instructor instructor) {
        return instructorRepository.createInstructor(instructor);
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
    public Instructor updateProfilePicture(Long instructorId, String profilePicture) {
        return instructorRepository.updateProfilePicture(instructorId, profilePicture);
    }
}
