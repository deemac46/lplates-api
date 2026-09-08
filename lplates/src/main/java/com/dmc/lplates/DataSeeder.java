package com.dmc.lplates;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.EdtProgress;
import com.dmc.lplates.inbound.models.Feedback;
import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.inbound.models.InstructorPricing;
import com.dmc.lplates.inbound.models.Role;
import com.dmc.lplates.inbound.models.User;
import com.dmc.lplates.service.BookingService;
import com.dmc.lplates.service.EdtProgressService;
import com.dmc.lplates.service.FeedbackService;
import com.dmc.lplates.service.InstructorPricingService;
import com.dmc.lplates.service.InstructorsService;
import com.dmc.lplates.service.UserService;

@Component
public class DataSeeder implements ApplicationRunner {

    private final InstructorsService instructorsService;
    private final BookingService bookingService;
    private final FeedbackService feedbackService;
    private final EdtProgressService edtProgressService;
        private final InstructorPricingService pricingService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final String dbUrl;

        private static final Map<Integer, BigDecimal> DEFAULT_PRICING_TIERS = Map.of(
            30, new BigDecimal("35.00"),
            45, new BigDecimal("50.00"),
            60, new BigDecimal("60.00"),
            90, new BigDecimal("85.00"),
            120, new BigDecimal("110.00")
        );

    @Value("${mockdata.enabled:false}")
    private boolean mockDataEnabled;

    public DataSeeder(InstructorsService instructorsService, BookingService bookingService,
                      FeedbackService feedbackService, EdtProgressService edtProgressService,
                      InstructorPricingService pricingService,
                      UserService userService, PasswordEncoder passwordEncoder,
                      @Value("${app.database.url}") String dbUrl) {
        this.instructorsService = instructorsService;
        this.bookingService = bookingService;
        this.feedbackService = feedbackService;
        this.edtProgressService = edtProgressService;
        this.pricingService = pricingService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.dbUrl = dbUrl;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!mockDataEnabled) {
            System.out.println("DataSeeder: mockdata.enabled=false, skipping seed.");
            return;
        }

        boolean hasInstructorSeedData = instructorsService.getAllInstructors().size() >= 10;
        boolean hasLessonRows = !bookingService.getAllBookings().isEmpty();
        boolean hasLessonRowsWithInstructorIds = bookingService.getAllBookings().stream()
                .anyMatch(booking -> booking.getInstructorId() != null);

        if (hasInstructorSeedData && hasLessonRows && hasLessonRowsWithInstructorIds) {
            seedMissingInstructorPricing();
            System.out.println("DataSeeder: mock data already present, skipping seed.");
            return;
        }

        if (!hasInstructorSeedData) {
            seedInstructors();
            seedStudentUsers();
            seedAdditionalAdmin();
        } else if (hasLessonRows && !hasLessonRowsWithInstructorIds) {
            System.out.println("DataSeeder: existing lesson rows are missing instructor_ids; resetting lesson seed data.");
            resetMockLessonData();
        }

        seedMissingInstructorPricing();
        seedLessons();
        seedFeedback();
        seedEdtProgress();
    }

    private void seedMissingInstructorPricing() {
        int createdPricingRows = 0;
        for (Instructor instructor : instructorsService.getAllInstructors()) {
            if (instructor.getInstructorId() == null || !"approved".equalsIgnoreCase(instructor.getApprovalStatus())) {
                continue;
            }

            Set<Integer> existingDurations = new HashSet<>();
            for (InstructorPricing existing : pricingService.getPricingByInstructorId(instructor.getInstructorId())) {
                if (existing.getDurationMinutes() != null) {
                    existingDurations.add(existing.getDurationMinutes());
                }
            }

            for (Map.Entry<Integer, BigDecimal> tier : DEFAULT_PRICING_TIERS.entrySet()) {
                if (existingDurations.contains(tier.getKey())) {
                    continue;
                }
                InstructorPricing pricing = new InstructorPricing();
                pricing.setInstructorId(instructor.getInstructorId());
                pricing.setDurationMinutes(tier.getKey());
                pricing.setPrice(tier.getValue());
                pricingService.createPricing(pricing);
                createdPricingRows++;
            }
        }

        if (createdPricingRows > 0) {
            System.out.println("DataSeeder: backfilled " + createdPricingRows + " instructor pricing rows.");
        }
    }

    private void resetMockLessonData() {
        String[] tables = {"bookings_feedback", "bookings_edtprogress", "bookings_lesson"};
        try (Connection connection = DriverManager.getConnection(dbUrl);
             Statement statement = connection.createStatement()) {
            for (String table : tables) {
                statement.executeUpdate("DELETE FROM \"" + table + "\"");
            }
        } catch (SQLException e) {
            System.err.println("DataSeeder: failed to reset mock lesson data: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Instructors — 12 Dublin, 7 Galway, 6 Cork
    // userId acts as a reference to the user account (101-125)
    // -------------------------------------------------------------------------
    private void seedInstructors() {
        List<Instructor> instructors = List.of(

            // --- Dublin (12) ---
            instructor(101L, "ADI-001", "approved", "male",   "Seán",     "Murphy",      "sean.murphy@lplates.ie",        "0851234567", "manual",    8,  4.8, "Toyota",     "Corolla", "Drumcondra,Glasnevin,Phibsborough",         "Dublin", "Drumcondra, Glasnevin, Phibsborough", 53.3698, -6.2603, "Experienced manual instructor in North Dublin",   42, false, null, false, null, null, false),
            instructor(102L, "ADI-002", "approved", "female", "Aoife",    "Kelly",       "aoife.kelly@lplates.ie",        "0852345678", "automatic", 5,  4.6, "Volkswagen", "Golf",    "Rathmines,Rathgar,Terenure",                "Dublin", "Rathmines, Rathgar, Terenure", 53.3178, -6.2639, "Automatic specialist in South Dublin",            28, true,  new BigDecimal("150.00"), false, null, null, false),
            instructor(103L, "ADI-003", "approved", "male",   "Ciarán",   "O'Brien",     "ciaran.obrien@lplates.ie",      "0853456789", "both",      12, 4.9, "Ford",       "Focus",   "Clontarf,Raheny,Dollymount",                "Dublin", "Clontarf, Raheny, Dollymount", 53.3712, -6.1951, "Top-rated instructor covering North Dublin coast", 71, true,  new BigDecimal("200.00"), false, null, "Hearing impairments, visual impairments", true),
            instructor(104L, "ADI-004", "approved", "female", "Niamh",    "Walsh",       "niamh.walsh@lplates.ie",        "0854567890", "manual",    3,  4.2, "Hyundai",    "i20",     "Swords,Malahide,Portmarnock",               "Dublin", "Swords, Malahide, Portmarnock", 53.4597, -6.2181, "North County Dublin specialist",                  15, false, null, false, null, null, false),
            instructor(105L, "ADI-005", "approved", "male",   "Pádraig",  "Byrne",       "padraig.byrne@lplates.ie",      "0855678901", "manual",    10, 4.7, "Skoda",      "Fabia",   "Tallaght,Rathfarnham,Templeogue",           "Dublin", "Tallaght, Rathfarnham, Templeogue", 53.2859, -6.3586, "South West Dublin expert with 10 years experience",55, false, null, false, null, null, false),
            instructor(106L, "ADI-006", "approved", "female", "Siobhán",  "Doyle",       "siobhan.doyle@lplates.ie",      "0856789012", "automatic", 6,  4.4, "Renault",    "Clio",    "Blanchardstown,Castleknock,Carpenterstown","Dublin", "Blanchardstown, Castleknock, Carpenterstown", 53.3879, -6.3784, "West Dublin automatic lessons",                   30, true,  new BigDecimal("160.00"), false, null, null, false),
            instructor(107L, "ADI-007", "approved", "male",   "Declan",   "Fitzgerald",  "declan.fitzgerald@lplates.ie",  "0857890123", "both",      9,  4.5, "Nissan",     "Micra",   "Dún Laoghaire,Blackrock,Monkstown",         "Dublin", "Dún Laoghaire, Blackrock, Monkstown", 53.2941, -6.1337, "South County Dublin instructor",                  38, true,  new BigDecimal("180.00"), false, null, null, false),
            instructor(108L, "ADI-008", "approved", "female", "Aisling",  "Ryan",        "aisling.ryan@lplates.ie",       "0858901234", "manual",    4,  4.3, "Toyota",     "Yaris",   "Finglas,Cabra,Glasnevin",                   "Dublin", "Finglas, Cabra, Glasnevin", 53.3892, -6.2997, "North West Dublin lessons",                       20, false, null, false, null, null, false),
            instructor(109L, "ADI-009", "approved", "male",   "Tomás",    "Lynch",       "tomas.lynch@lplates.ie",        "0859012345", "manual",    7,  4.6, "Ford",       "Fiesta",  "Lucan,Clondalkin,Palmerstown",              "Dublin", "Lucan, Clondalkin, Palmerstown", 53.3529, -6.4486, "West Dublin manual instructor",                   35, false, null, false, null, null, false),
            instructor(110L, "ADI-010", "approved", "female", "Caoimhe",  "O'Sullivan",  "caoimhe.osullivan@lplates.ie",  "0850123456", "automatic", 11, 4.8, "Volkswagen", "Polo",    "Ballsbridge,Donnybrook,Sandymount",         "Dublin", "Ballsbridge, Donnybrook, Sandymount", 53.3294, -6.2281, "Southside Dublin automatic specialist",            60, true,  new BigDecimal("200.00"), true, "Hand controls, left-foot accelerator", "Autism, ADHD", true),
            instructor(111L, "ADI-011", "approved", "male",   "Ronan",    "McCarthy",    "ronan.mccarthy@lplates.ie",     "0861234567", "manual",    2,  4.1, "Opel",       "Corsa",   "Santry,Beaumont,Artane",                    "Dublin", "Santry, Beaumont, Artane", 53.3948, -6.2451, "North Dublin beginner-friendly instructor",        8,  false, null, false, null, null, false),
            instructor(112L, "ADI-012", "approved", "female", "Éadaoin",  "Brennan",     "eadaoin.brennan@lplates.ie",    "0862345678", "both",      8,  4.5, "Peugeot",    "208",     "Crumlin,Walkinstown,Drimnagh",              "Dublin", "Crumlin, Walkinstown, Drimnagh", 53.3235, -6.3168, "South Dublin dual-transmission instructor",        41, false, null, false, null, null, false),

            // --- Galway (7) ---
            instructor(113L, "ADI-013", "approved", "female", "Máire",    "Connolly",    "maire.connolly@lplates.ie",     "0863456789", "manual",    6,  4.7, "Toyota",     "Corolla", "Salthill,Galway City,Knocknacarra",         "Galway", "Salthill, Galway City, Knocknacarra", 53.2707, -9.0568, "Galway city and seaside lessons",                 33, false, null, false, null, null, false),
            instructor(114L, "ADI-014", "approved", "male",   "Fearghus", "O'Connor",    "fearghus.oconnor@lplates.ie",   "0864567890", "automatic", 9,  4.5, "Volkswagen", "Golf",    "Oranmore,Athenry,Claregalway",              "Galway", "Oranmore, Athenry, Claregalway", 53.2649, -8.9234, "East Galway automatic instructor",                45, true,  new BigDecimal("170.00"), false, null, null, false),
            instructor(115L, "ADI-015", "approved", "female", "Sorcha",   "Burke",       "sorcha.burke@lplates.ie",       "0865678901", "manual",    4,  4.3, "Hyundai",    "i20",     "Tuam,Claregalway,Headford",                 "Galway", "Tuam, Claregalway, Headford", 53.5145, -8.8512, "North Galway area instructor",                    18, false, null, false, null, null, false),
            instructor(116L, "ADI-016", "approved", "male",   "Cormac",   "Flaherty",    "cormac.flaherty@lplates.ie",    "0866789012", "both",      14, 4.9, "Ford",       "Focus",   "Galway City,Salthill,Renmore",              "Galway", "Galway City, Salthill, Renmore", 53.2707, -9.0491, "Senior Galway city instructor",                   78, true,  new BigDecimal("220.00"), false, null, "Autism, learning difficulties", true),
            instructor(117L, "ADI-017", "approved", "female", "Bríd",     "Naughton",    "brid.naughton@lplates.ie",      "0867890123", "manual",    5,  4.4, "Skoda",      "Fabia",   "Ballinasloe,Loughrea,Portumna",             "Galway", "Ballinasloe, Loughrea, Portumna", 53.3277, -8.2213, "East Galway rural instructor",                    22, false, null, false, null, null, false),
            instructor(118L, "ADI-018", "approved", "male",   "Tadhg",    "Madden",      "tadhg.madden@lplates.ie",       "0868901234", "automatic", 7,  4.6, "Renault",    "Clio",    "Oranmore,Galway City,Merlin Park",          "Galway", "Oranmore, Galway City, Merlin Park", 53.2649, -9.0153, "South Galway automatic lessons",                  37, true,  new BigDecimal("160.00"), false, null, null, false),
            instructor(119L, "ADI-019", "pending",  "female", "Orla",     "King",        "orla.king@lplates.ie",          "0869012345", "manual",    3,  4.2, "Nissan",     "Micra",   "Galway City,Knocknacarra,Westside",         "Galway", "Galway City, Knocknacarra, Westside", 53.2768, -9.0738, "Galway city beginner-friendly instructor",         11, false, null, false, null, null, false),

            // --- Cork (6) ---
            instructor(120L, "ADI-020", "approved", "male",   "Donal",    "Collins",     "donal.collins@lplates.ie",      "0871234567", "manual",    10, 4.7, "Toyota",     "Corolla", "Cork City,Bishopstown,Wilton",              "Cork", "Cork City, Bishopstown, Wilton", 51.8985, -8.4756, "Cork city manual driving instructor",              53, false, null, false, null, null, false),
            instructor(121L, "ADI-021", "approved", "female", "Catriona", "O'Callaghan", "catriona.ocallaghan@lplates.ie","0872345678", "automatic", 8,  4.5, "Volkswagen", "Polo",    "Ballincollig,Togher,Bishopstown",           "Cork", "Ballincollig, Togher, Bishopstown", 51.8874, -8.5888, "West Cork automatic specialist",                  40, true,  new BigDecimal("160.00"), false, null, null, false),
            instructor(122L, "ADI-022", "approved", "female", "Fionnuala","Cronin",      "fionnuala.cronin@lplates.ie",   "0873456789", "both",      6,  4.4, "Ford",       "Fiesta",  "Cobh,Carrigaline,Passage West",             "Cork", "Cobh, Carrigaline, Passage West", 51.8508, -8.3025, "Harbour towns dual-transmission instructor",       26, false, null, false, null, null, false),
            instructor(123L, "ADI-023", "approved", "male",   "Seamus",   "Healy",       "seamus.healy@lplates.ie",       "0874567890", "manual",    11, 4.8, "Skoda",      "Octavia", "Midleton,Carrigaline,Cobh",                 "Cork", "Midleton, Carrigaline, Cobh", 51.9152, -8.1789, "East Cork experienced instructor",                 58, true,  new BigDecimal("190.00"), false, null, null, false),
            instructor(124L, "ADI-024", "approved", "female", "Mairéad",  "O'Driscoll",  "mairead.odriscoll@lplates.ie",  "0875678901", "automatic", 5,  4.3, "Hyundai",    "i30",     "Cork City,Blackrock,Mahon",                 "Cork", "Cork City, Blackrock, Mahon", 51.8974, -8.4136, "Cork south docklands automatic lessons",           23, false, null, false, null, null, false),
            instructor(125L, "ADI-025", "approved", "male",   "Liam",     "Horgan",      "liam.horgan@lplates.ie",        "0876789012", "manual",    7,  4.6, "Opel",       "Astra",   "Bishopstown,Wilton,Model Farm Road",        "Cork", "Bishopstown, Wilton, Model Farm Road", 51.8815, -8.5295, "West Cork city instructor",                        36, false, null, false, null, null, false)
        );

        instructors.forEach(instructorsService::createInstructor);
        seedInstructorUsers(instructors);
        System.out.println("DataSeeder: seeded " + instructors.size() + " instructors.");
    }

    // -------------------------------------------------------------------------
    // accounts_user — one INSTRUCTOR-role account per seeded instructor, sharing
    // the same ID (101-125) so /users/me/profile resolves correctly when logged in.
    // -------------------------------------------------------------------------
    private void seedInstructorUsers(List<Instructor> instructors) {
        for (Instructor instructor : instructors) {
            User user = new User();
            user.setUsername(instructor.getEmail().substring(0, instructor.getEmail().indexOf('@')));
            user.setFirstName(instructor.getFirstName());
            user.setLastName(instructor.getLastName());
            user.setEmail(instructor.getEmail());
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.INSTRUCTOR);
            user.setActive(true);
            userService.createUserWithId(instructor.getUserId(), user);
        }
        System.out.println("DataSeeder: seeded " + instructors.size() + " instructor user accounts.");
    }

    // -------------------------------------------------------------------------
    // accounts_user — LEARNER-role accounts for the student IDs (201-210) referenced
    // by seeded lessons and EDT progress.
    // -------------------------------------------------------------------------
    private void seedStudentUsers() {
        learnerUser(201L, "emer.nolan", "Emer", "Nolan", "emer.nolan@example.ie");
        learnerUser(202L, "cian.brady", "Cian", "Brady", "cian.brady@example.ie");
        learnerUser(203L, "laura.fitzpatrick", "Laura", "Fitzpatrick", "laura.fitzpatrick@example.ie");
        learnerUser(204L, "darragh.kenny", "Darragh", "Kenny", "darragh.kenny@example.ie");
        learnerUser(205L, "roisin.hayes", "Róisín", "Hayes", "roisin.hayes@example.ie");
        learnerUser(206L, "conor.whelan", "Conor", "Whelan", "conor.whelan@example.ie");
        learnerUser(207L, "aine.sheehan", "Áine", "Sheehan", "aine.sheehan@example.ie");
        learnerUser(208L, "barry.nugent", "Barry", "Nugent", "barry.nugent@example.ie");
        learnerUser(209L, "grainne.dunne", "Gráinne", "Dunne", "grainne.dunne@example.ie");
        learnerUser(210L, "eoin.carroll", "Eoin", "Carroll", "eoin.carroll@example.ie");
        System.out.println("DataSeeder: seeded 10 learner user accounts.");
    }

    private void learnerUser(long id, String username, String firstName, String lastName, String email) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.LEARNER);
        user.setActive(true);
        userService.createUserWithId(id, user);
    }

    // -------------------------------------------------------------------------
    // An extra ADMIN account alongside the bootstrap "admin" user created by
    // UserRepositoryHandler on first startup.
    // -------------------------------------------------------------------------
    private void seedAdditionalAdmin() {
        if (userService.existsByUsername("ops_admin")) {
            return;
        }
        User admin = new User();
        admin.setUsername("ops_admin");
        admin.setFirstName("Operations");
        admin.setLastName("Admin");
        admin.setEmail("ops.admin@lplates.ie");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        userService.createUser(admin);
        System.out.println("DataSeeder: seeded additional admin user (username=ops_admin, password=admin123).");
    }

    // -------------------------------------------------------------------------
    // Lessons — 8 instructors: 1-12 (Dublin), 13 (Galway), 17 (Galway), 20 (Cork)
    // studentId values are arbitrary user IDs 201-210
    // -------------------------------------------------------------------------
    private void seedLessons() {
        LocalDate base = LocalDate.of(2026, 5, 20);
        LocalTime morning = LocalTime.of(9, 0);
        LocalTime midday  = LocalTime.of(11, 0);
        LocalTime afternoon = LocalTime.of(14, 0);
        LocalTime evening = LocalTime.of(16, 0);

        // Instructor 1
        lesson(1L, 201L, base,             morning,   60,  "confirmed", "paid",   new BigDecimal("60.00"),  "lesson", "", "Lesson 1 with instructor 1");
        lesson(1L, 202L, base.plusDays(2), midday,    60,  "pending",   "unpaid", new BigDecimal("60.00"),  "lesson", "", "Lesson 2 with instructor 1");

        // Instructor 2
        lesson(2L, 203L, base.plusDays(1), midday,    90,  "confirmed", "paid",   new BigDecimal("85.00"),  "lesson", "", "First lesson with instructor 2");
        lesson(2L, 204L, base.plusDays(3), afternoon, 60,  "confirmed", "paid",   new BigDecimal("60.00"),  "lesson", "", "Follow-up with instructor 2");

        // Instructor 3
        lesson(3L, 205L, base.plusDays(1), morning,   120, "confirmed", "paid",   new BigDecimal("110.00"), "lesson", "", "Extended session with instructor 3");
        lesson(3L, 201L, base.plusDays(4), evening,   60,  "pending",   "unpaid", new BigDecimal("60.00"),  "lesson", "", "Evening lesson with instructor 3");
        lesson(3L, 206L, base.plusDays(6), midday,    60,  "confirmed", "paid",   new BigDecimal("60.00"),  "lesson", "", "Weekend lesson with instructor 3");

        // Instructor 5
        lesson(5L, 207L, base.plusDays(2), afternoon, 60,  "confirmed", "paid",   new BigDecimal("60.00"),  "lesson", "", "South Dublin lesson with instructor 5");
        lesson(5L, 208L, base.plusDays(7), morning,   90,  "pending",   "unpaid", new BigDecimal("85.00"),  "lesson", "", "Upcoming lesson with instructor 5");

        // Instructor 8
        lesson(8L, 209L, base.plusDays(3), midday,    60,  "confirmed", "paid",   new BigDecimal("60.00"),  "lesson", "", "Lesson with instructor 8");
        lesson(8L, 210L, base.plusDays(8), afternoon, 60,  "pending",   "unpaid", new BigDecimal("60.00"),  "lesson", "", "Upcoming lesson with instructor 8");

        // Instructor 13
        lesson(13L, 201L, base.plusDays(1), midday,    60, "confirmed", "paid",   new BigDecimal("60.00"),  "lesson", "", "Galway lesson with instructor 13");
        lesson(13L, 203L, base.plusDays(4), afternoon, 90, "confirmed", "paid",   new BigDecimal("85.00"),  "lesson", "", "Extended Galway session with instructor 13");
        lesson(13L, 205L, base.plusDays(9), morning,   60, "pending",   "unpaid", new BigDecimal("60.00"),  "lesson", "", "Upcoming Salthill lesson with instructor 13");

        // Instructor 17
        lesson(17L, 202L, base.plusDays(2), midday,    60, "confirmed", "paid",   new BigDecimal("55.00"),  "lesson", "", "East Galway lesson with instructor 17");
        lesson(17L, 207L, base.plusDays(6), afternoon, 60, "pending",   "unpaid", new BigDecimal("55.00"),  "lesson", "", "Upcoming lesson with instructor 17");

        // Instructor 20
        lesson(20L, 204L, base.plusDays(3), midday,    60,  "confirmed",  "paid",      new BigDecimal("60.00"),  "lesson", "", "Cork lesson with instructor 20");
        lesson(20L, 208L, base.plusDays(5), afternoon, 90,  "confirmed",  "paid",      new BigDecimal("85.00"),  "lesson", "", "Extended Cork session with instructor 20");
        lesson(20L, 209L, base.plusDays(7), morning,   60,  "cancelled",  "refunded",  new BigDecimal("60.00"),  "lesson", "", "Cancelled Cork lesson");

        // EDT lessons for student 201 (modules 1-6 completed, 7-12 upcoming)
        edtLesson(1L, 201L, base.minusDays(60), morning,   60, "confirmed", "paid", new BigDecimal("60.00"), "1", true);
        edtLesson(1L, 201L, base.minusDays(53), morning,   60, "confirmed", "paid", new BigDecimal("60.00"), "2", true);
        edtLesson(1L, 201L, base.minusDays(46), morning,   60, "confirmed", "paid", new BigDecimal("60.00"), "3", true);
        edtLesson(1L, 201L, base.minusDays(39), morning,   60, "confirmed", "paid", new BigDecimal("60.00"), "4", true);
        edtLesson(1L, 201L, base.minusDays(32), morning,   60, "confirmed", "paid", new BigDecimal("60.00"), "5", true);
        edtLesson(1L, 201L, base.minusDays(25), morning,   60, "confirmed", "paid", new BigDecimal("60.00"), "6", true);
        edtLesson(1L, 201L, base.plusDays(14),  morning,   60, "pending",   "unpaid", new BigDecimal("60.00"), "7", false);
        edtLesson(1L, 201L, base.plusDays(21),  morning,   60, "pending",   "unpaid", new BigDecimal("60.00"), "8", false);
        edtLesson(1L, 201L, base.plusDays(28),  morning,   60, "pending",   "unpaid", new BigDecimal("60.00"), "9", false);
        edtLesson(1L, 201L, base.plusDays(35),  morning,   60, "pending",   "unpaid", new BigDecimal("60.00"), "10", false);
        edtLesson(1L, 201L, base.plusDays(42),  morning,   60, "pending",   "unpaid", new BigDecimal("60.00"), "11", false);
        edtLesson(1L, 201L, base.plusDays(49),  morning,   60, "pending",   "unpaid", new BigDecimal("60.00"), "12", false);

        // EDT lessons for student 202 (modules 1-3 completed)
        edtLesson(2L, 202L, base.minusDays(30), midday, 60, "confirmed", "paid", new BigDecimal("60.00"), "1", true);
        edtLesson(2L, 202L, base.minusDays(23), midday, 60, "confirmed", "paid", new BigDecimal("60.00"), "2", true);
        edtLesson(2L, 202L, base.minusDays(16), midday, 60, "confirmed", "paid", new BigDecimal("60.00"), "3", true);

        System.out.println("DataSeeder: seeded lessons for 7 instructors.");
    }

    // -------------------------------------------------------------------------
    // Feedback — for confirmed/paid lessons (IDs assigned sequentially by DB)
    // Lesson IDs match insertion order: 1,3,4,5,7,8,10,12,13,15,17,18
    // authorId = studentId from the corresponding lesson
    // -------------------------------------------------------------------------
    private void seedFeedback() {
        feedback(1L,  201L, 5, "Great first lesson, very patient instructor!");
        feedback(3L,  203L, 4, "Good lesson, clear instructions.");
        feedback(4L,  204L, 5, "Excellent follow-up session.");
        feedback(5L,  205L, 5, "Extended session was really worth it.");
        feedback(7L,  206L, 4, "Productive weekend lesson.");
        feedback(8L,  207L, 4, "Really helpful South Dublin lesson.");
        feedback(10L, 209L, 5, "Fantastic lesson, highly recommend!");
        feedback(12L, 201L, 4, "Great Galway lesson by the sea.");
        feedback(13L, 203L, 5, "Extended Galway session was excellent.");
        feedback(15L, 202L, 4, "Solid East Galway lesson.");
        feedback(17L, 204L, 5, "Cork lesson was very well structured.");
        feedback(18L, 208L, 4, "Good extended Cork session.");
        System.out.println("DataSeeder: seeded feedback for 12 lessons.");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private void feedback(long lessonId, long authorId, int rating, String comment) {
        Feedback f = new Feedback();
        f.setLessonId(lessonId);
        f.setAuthorId(authorId);
        f.setRating(rating);
        f.setComment(comment);
        f.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        feedbackService.createFeedback(f);
    }

    private void lesson(long instructorId, long studentId, LocalDate date, LocalTime time,
                        int durationMinutes, String status, String paymentStatus,
                        BigDecimal price, String lessonType, String edtModule, String notes) {
        Booking b = new Booking();
        b.setInstructorId(instructorId);
        b.setStudentId(studentId);
        b.setScheduledDate(date);
        b.setScheduledTime(time);
        b.setDurationMinutes(durationMinutes);
        b.setStatus(status);
        b.setPaymentStatus(paymentStatus);
        b.setPrice(price);
        b.setCurrency("EUR");
        b.setLessonType(lessonType);
        b.setNotes(notes);
        b.setEdtModule(edtModule != null ? edtModule : "");
        b.setEdtCompleted(false);
        b.setCancellationReason("");
        b.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        b.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        bookingService.createBooking(b);
    }

    private void edtLesson(long instructorId, long studentId, LocalDate date, LocalTime time,
                           int durationMinutes, String status, String paymentStatus,
                           BigDecimal price, String edtModule, boolean edtCompleted) {
        Booking b = new Booking();
        b.setInstructorId(instructorId);
        b.setStudentId(studentId);
        b.setScheduledDate(date);
        b.setScheduledTime(time);
        b.setDurationMinutes(durationMinutes);
        b.setStatus(status);
        b.setPaymentStatus(paymentStatus);
        b.setPrice(price);
        b.setCurrency("EUR");
        b.setLessonType("edt");
        b.setNotes("EDT Module " + edtModule);
        b.setEdtModule(edtModule);
        b.setEdtCompleted(edtCompleted);
        b.setCancellationReason("");
        b.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        b.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        bookingService.createBooking(b);
    }

    private Instructor instructor(Long userId, String adiNumber, String approvalStatus, String gender,
                                  String firstName, String lastName, String email,
                                  String phoneNumber, String transmission, int yearsExperience, double rating,
                                  String carMake, String carModel, String locationsCsv,
                                  String county, String areasCovered, Double latitude, Double longitude,
                                  String description, int reviewsCount,
                                  boolean offersTestCarHire, BigDecimal testCarHirePrice,
                                  boolean hasAdaptedVehicle, String adaptedVehicleTypes,
                                  String disabilityExperience, boolean disabilityTraining) {
        Instructor i = new Instructor();
        i.setUserId(userId);
        i.setAdiNumber(adiNumber);
        i.setApprovalStatus(approvalStatus);
        i.setGender(gender);
        i.setFirstName(firstName);
        i.setLastName(lastName);
        i.setEmail(email);
        i.setPhoneNumber(phoneNumber);
        i.setTransmission(transmission);
        i.setYearsExperience(yearsExperience);
        i.setRating(rating);
        i.setCarMake(carMake);
        i.setCarModel(carModel);
        i.setLocations(List.of(locationsCsv.split(",")));
        i.setCounty(county);
        i.setAreasCovered(areasCovered);
        i.setLatitude(latitude);
        i.setLongitude(longitude);
        i.setDescription(description);
        i.setReviewsCount(reviewsCount);
        i.setAgreeTerms(true);
        i.setOffersTestCarHire(offersTestCarHire);
        i.setTestCarHirePrice(testCarHirePrice);
        i.setHasAdaptedVehicle(hasAdaptedVehicle);
        i.setAdaptedVehicleTypes(adaptedVehicleTypes);
        i.setDisabilityExperience(disabilityExperience);
        i.setDisabilityTraining(disabilityTraining);
        i.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        i.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return i;
    }

    // -------------------------------------------------------------------------
    // EDT Module names (Irish Road Safety Authority official modules)
    // -------------------------------------------------------------------------
    private static final String[] EDT_MODULE_NAMES = {
        "",
        "Car Familiarisation",
        "Correct Position",
        "Basic Manoeuvres",
        "Changing Direction",
        "Moving Off and Stopping",
        "Awareness and Anticipation",
        "Reaction Time",
        "Overtaking",
        "Speed Management",
        "Night Driving",
        "Motorway Driving",
        "Eco Driving"
    };

    // -------------------------------------------------------------------------
    // EDT Progress — seed module records for students 201 (6 done) and 202 (3 done)
    // -------------------------------------------------------------------------
    private void seedEdtProgress() {
        // Student 201 — modules 1-6 completed
        for (int m = 1; m <= 12; m++) {
            EdtProgress p = new EdtProgress();
            p.setStudentId(201L);
            p.setModuleNumber(m);
            p.setModuleName(EDT_MODULE_NAMES[m]);
            p.setCompleted(m <= 6);
            p.setCompletedAt(m <= 6 ? new Timestamp(System.currentTimeMillis()) : null);
            edtProgressService.createEdtProgress(p);
        }
        // Student 202 — modules 1-3 completed
        for (int m = 1; m <= 12; m++) {
            EdtProgress p = new EdtProgress();
            p.setStudentId(202L);
            p.setModuleNumber(m);
            p.setModuleName(EDT_MODULE_NAMES[m]);
            p.setCompleted(m <= 3);
            p.setCompletedAt(m <= 3 ? new Timestamp(System.currentTimeMillis()) : null);
            edtProgressService.createEdtProgress(p);
        }
        System.out.println("DataSeeder: seeded EDT progress for students 201 and 202.");
    }
}

