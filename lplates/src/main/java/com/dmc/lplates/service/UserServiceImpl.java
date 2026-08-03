package com.dmc.lplates.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dmc.lplates.database.repository.UserRepository;
import com.dmc.lplates.inbound.dtos.InstructorProfile;
import com.dmc.lplates.inbound.dtos.LearnerProfile;
import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.inbound.models.Role;
import com.dmc.lplates.inbound.models.User;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final EdtProgressService edtProgressService;
    private final InstructorsService instructorsService;

    public UserServiceImpl(UserRepository userRepository,
                           BookingService bookingService,
                           EdtProgressService edtProgressService,
                           InstructorsService instructorsService) {
        this.userRepository = userRepository;
        this.bookingService = bookingService;
        this.edtProgressService = edtProgressService;
        this.instructorsService = instructorsService;
    }

    @Override
    public User createUser(User user) {
        return userRepository.insertUser(user);
    }

    @Override
    public User createUserWithId(long id, User user) {
        return userRepository.insertUserWithId(id, user);
    }

    @Override
    public User getUserById(long id) {
        return userRepository.findById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getUserByUsernameOrEmail(String identifier) {
        User user = userRepository.findByUsername(identifier);
        return user != null ? user : userRepository.findByEmail(identifier);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public LearnerProfile getLearnerProfile(long userId) {
        User user = userRepository.findById(userId);
        if (user == null || user.getRole() != Role.LEARNER) return null;
        return new LearnerProfile(
                user,
                bookingService.getLessonsByStudentId(userId),
                edtProgressService.getEdtProgressByStudentId(userId)
        );
    }

    @Override
    public InstructorProfile getInstructorProfile(long userId) {
        User user = userRepository.findById(userId);
        if (user == null || user.getRole() != Role.INSTRUCTOR) return null;
        // Load instructor with their lessons included
        Instructor instructor = instructorsService.getInstructorByUserId(userId);
        if (instructor != null && instructor.getInstructorId() != null) {
            instructor = instructorsService.getInstructorWithLessons(instructor.getInstructorId());
        }
        return new InstructorProfile(user, instructor);
    }
}
