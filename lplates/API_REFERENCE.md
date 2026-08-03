# lplates REST API — Client Integration Reference

**Base URL:** `http://localhost:8080`  
**Auth:** JWT Bearer token (HS256)  
**Content-Type:** `application/json`

---

## Authentication

All endpoints except `/auth/**` and `/swagger-ui/**` require the header:
```
Authorization: Bearer <token>
```

---

## 1. Auth Endpoints (public)

### POST /auth/register
Create a new user account. Returns a JWT on success.

**Request body:**
```json
{
  "username":  "john_doe",
  "firstName": "John",
  "lastName":  "Doe",
  "email":     "john@example.ie",
  "password":  "secret123",
  "role":      "LEARNER"
}
```
`role` must be one of: `LEARNER`, `INSTRUCTOR`, `ADMIN`. Defaults to `LEARNER` if omitted.

**201 Response:**
```json
{
  "token":    "eyJhbGciOiJIUzI1NiJ9...",
  "type":     "Bearer",
  "userId":   5,
  "username": "john_doe",
  "role":     "LEARNER"
}
```

**409 Conflict** — username or email already in use.

---

### POST /auth/login
Exchange credentials for a JWT.

**Request body:**
```json
{
  "username": "john_doe",
  "password": "secret123"
}
```

**200 Response:** same shape as `/auth/register`.

**401** — invalid credentials.

**Default admin account (seeded on first startup):**
- username: `admin`
- password: `admin123`

---

## 2. User Endpoints

### GET /users/me
Returns the currently authenticated user's basic info.

**200 Response:**
```json
{
  "id":        5,
  "username":  "john_doe",
  "firstName": "John",
  "lastName":  "Doe",
  "email":     "john@example.ie",
  "role":      "LEARNER",
  "active":    true,
  "createdAt": "2026-07-19T10:00:00.000+00:00"
}
```
Note: `password` is never returned.

---

### GET /users/me/profile
Returns a role-specific profile. Shape varies by role (see below).

#### LEARNER profile response:
```json
{
  "user": { "id": 5, "username": "john_doe", "role": "LEARNER", ... },
  "bookings": [
    {
      "lessonId":     1,
      "instructorId": 2,
      "studentId":    5,
      "scheduledDate": "2026-08-01",
      "scheduledTime": "09:00:00",
      "durationMinutes": 60,
      "status":       "confirmed",
      "paymentStatus": "paid",
      "price":        60.00,
      "currency":     "EUR",
      "lessonType":   "lesson",
      "edtModule":    "",
      "edtCompleted": false,
      "notes":        ""
    }
  ],
  "edtProgress": [
    {
      "id":           1,
      "studentId":    5,
      "moduleNumber": 1,
      "moduleName":   "Car Familiarisation",
      "completed":    true,
      "lessonId":     7,
      "completedAt":  "2026-06-01T09:00:00.000+00:00"
    }
  ],
  "edtSummary": {
    "totalModules":     12,
    "completedModules": 6,
    "remainingModules": 6,
    "fullyCompleted":   false
  }
}
```

#### INSTRUCTOR profile response:
```json
{
  "user": { "id": 3, "username": "sean_m", "role": "INSTRUCTOR", ... },
  "instructor": {
    "instructorId":      1,
    "userId":            3,
    "firstName":         "Sean",
    "lastName":          "Murphy",
    "email":             "sean@lplates.ie",
    "approvalStatus":    "approved",
    "gender":            "male",
    "transmission":      "manual",
    "rating":            4.8,
    "carMake":           "Toyota",
    "carModel":          "Corolla",
    "locations":         ["Drumcondra", "Glasnevin"],
    "yearsExperience":   8,
    "reviewsCount":      42,
    "offersTestCarHire": false,
    "testCarHirePrice":  null,
    "lessons": [ ... ]
  }
}
```

#### ADMIN response:
Plain `User` object (same as `/users/me`).

---

### GET /users/{id}  *(ADMIN only)*
Returns basic user info for any user by ID.

### GET /users/  *(ADMIN only)*
Returns all users as an array.

### GET /users/{id}/profile  *(ADMIN only)*
Returns role-specific profile for any user.

---

## 3. Instructor Endpoints  *(authenticated)*

### GET /instructors/
Returns all instructors.

### GET /instructors/{instructorId}
Returns a single instructor by their instructor ID (not user ID).

### POST /instructors/create  *(INSTRUCTOR, ADMIN)*
Creates an instructor profile. The `userId` field must match the authenticated user's ID.

**Request body:**
```json
{
  "userId":            3,
  "firstName":         "Sean",
  "lastName":          "Murphy",
  "email":             "sean@lplates.ie",
  "adiNumber":         "ADI-001",
  "approvalStatus":    "pending",
  "gender":            "male",
  "phoneNumber":       "0851234567",
  "transmission":      "manual",
  "yearsExperience":   8,
  "rating":            0.0,
  "carMake":           "Toyota",
  "carModel":          "Corolla",
  "locations":         ["Drumcondra", "Glasnevin"],
  "description":       "Experienced instructor",
  "reviewsCount":      0,
  "agreeTerms":        true,
  "offersTestCarHire": false,
  "testCarHirePrice":  null
}
```

### GET /instructors/{instructorId}/lessons
Returns the instructor object with their full lesson list embedded.

### GET /instructors/pending  *(ADMIN only)*
Returns all instructors with `approvalStatus` = `pending`.

### PATCH /instructors/{instructorId}/approval  *(ADMIN only)*
Approve or reject an instructor sign-up.

**Request body:**
```json
{ "approvalStatus": "approved" }
```
`approvalStatus` must be one of: `pending`, `approved`, `rejected`.

**200 Response:** the updated `Instructor` object.
**404** — instructor not found. **400** — invalid `approvalStatus`.

### POST /instructors/{instructorId}/profile-picture  *(owning INSTRUCTOR or ADMIN)*
Uploads a profile picture. `multipart/form-data` with a `file` part (JPEG/PNG/WEBP, max 5MB).

**200 Response:** the updated `Instructor` object, with `profilePicture` set to a URL such as
`/uploads/instructors/{instructorId}/<generated-name>.jpg`, servable directly from the API's base URL.

**403** — not the owning instructor and not an ADMIN. **415** — unsupported file type.

---

## 4. Lesson Endpoints  *(authenticated)*

### GET /lessons/
All lessons.

### GET /lessons/{lessonId}
Single lesson.

### POST /lessons/create  *(LEARNER, ADMIN)*
Book a new lesson. `studentId` should be the authenticated user's `id`.

**Request body:**
```json
{
  "instructorId":    2,
  "studentId":       5,
  "scheduledDate":   "2026-08-10",
  "scheduledTime":   "09:00:00",
  "durationMinutes": 60,
  "status":          "pending",
  "paymentStatus":   "unpaid",
  "price":           60.00,
  "currency":        "EUR",
  "lessonType":      "lesson",
  "edtModule":       "",
  "edtCompleted":    false,
  "notes":           ""
}
```
`lessonType` values: `lesson`, `edt`, `test_car_hire`. Use `test_car_hire` for bookings where the
learner just hires the instructor's car (e.g. for a test), not a taught lesson. Only instructors with
`offersTestCarHire: true` should be booked this way; use their `testCarHirePrice` as the `price`.

### PUT /lessons/update/{lessonId}
Full update of a lesson. Same body as create.

### POST /lessons/confirm/{lessonId}
Sets lesson status to `confirmed`.

### GET /lessons/pending
All lessons with status `pending`.

### GET /lessons/instructor/{instructorId}
All lessons for an instructor.

### GET /lessons/student/{studentId}
All lessons for a student (use the authenticated user's `id`).

---

## 5. EDT Progress Endpoints  *(LEARNER, ADMIN)*

Ireland's EDT consists of 12 mandatory modules. One record per student/module pair.

### GET /edt/student/{studentId}
All 12 module records for a student, ordered by `moduleNumber`.

### GET /edt/student/{studentId}/module/{moduleNumber}
Single module record. `moduleNumber` 1–12.

### POST /edt/create
Create or upsert an EDT progress record.

**Request body:**
```json
{
  "studentId":    5,
  "moduleNumber": 1,
  "moduleName":   "Car Familiarisation",
  "completed":    false,
  "lessonId":     null,
  "completedAt":  null
}
```

### PUT /edt/complete/{studentId}/{moduleNumber}/{lessonId}
Mark a module as completed and link it to the lesson that completed it.

### GET /edt/
All EDT progress records (all students).

### GET /edt/{id}
Single record by ID.

**EDT module names (1–12):**

| # | Module name               |
|---|---------------------------|
| 1 | Car Familiarisation       |
| 2 | Correct Position          |
| 3 | Basic Manoeuvres          |
| 4 | Changing Direction        |
| 5 | Moving Off and Stopping   |
| 6 | Awareness and Anticipation|
| 7 | Reaction Time             |
| 8 | Overtaking                |
| 9 | Speed Management          |
|10 | Night Driving             |
|11 | Motorway Driving          |
|12 | Eco Driving               |

---

## 6. Pricing Endpoints  *(INSTRUCTOR, ADMIN)*

### POST /pricing/create
```json
{
  "instructorId":    2,
  "durationMinutes": 60,
  "price":           60.00
}
```

### GET /pricing/{pricingId}
### GET /pricing/instructor/{instructorId}
### DELETE /pricing/{pricingId}

---

## 7. Feedback Endpoints  *(authenticated)*

### POST /feedback/create
```json
{
  "lessonId":  1,
  "authorId":  5,
  "rating":    5,
  "comment":   "Great lesson!"
}
```
`rating` is 1–5. One feedback per lesson (unique constraint).

### GET /feedback/{feedbackId}
### GET /feedback/lesson/{lessonId}
### GET /feedback/instructor/{instructorId}

---

## Database Tables (SQLite)

| Table                      | Key columns                                            |
|----------------------------|--------------------------------------------------------|
| `accounts_user`            | `id`, `username`, `role` (LEARNER/INSTRUCTOR/ADMIN)    |
| `accounts_instructor`      | `id`, `user_id` → links to `accounts_user.id`          |
| `bookings_lesson`          | `id`, `instructor_id`, `student_id` → both `accounts_user.id` |
| `bookings_instructorpricing`| `id`, `instructor_id` → `accounts_instructor.id`      |
| `bookings_feedback`        | `id`, `lesson_id` (unique), `author_id`               |
| `bookings_edtprogress`     | `id`, `student_id`, `module_number` (unique pair)     |

**Key ID relationships for the Django client:**
- `accounts_user.id` ↔ `bookings_lesson.student_id` (LEARNER bookings)
- `accounts_user.id` ↔ `accounts_instructor.user_id` (INSTRUCTOR profile lookup)
- `accounts_instructor.id` ↔ `bookings_lesson.instructor_id`
- `accounts_user.id` ↔ `bookings_edtprogress.student_id`

---

## Role Permission Summary

| Endpoint group   | LEARNER | INSTRUCTOR | ADMIN |
|------------------|---------|------------|-------|
| `/auth/**`       | public  | public     | public|
| `/instructors/**`| read    | read+create+own profile picture | full  |
| `/instructors/pending`, `/instructors/*/approval` | — | — | full |
| `/lessons/**`    | read+create | read   | full  |
| `/edt/**`        | full    | —          | full  |
| `/pricing/**`    | —       | full       | full  |
| `/feedback/**`   | full    | read       | full  |
| `/users/me`      | self    | self       | self  |
| `/users/**`      | self    | self       | full  |

---

## Django Migration Notes

1. **Replace Django session auth** with JWT header: store the token from `/auth/login` response and send `Authorization: Bearer <token>` on every request.
2. **User IDs change** — Django's `auth_user.id` does not map to `accounts_user.id` in the SQLite DB. Re-register users via `/auth/register` to get new IDs.
3. **`student_id` on lessons** = the `accounts_user.id` of the learner, not a separate model ID.
4. **Instructor profile** is a separate record from the user — after registering an INSTRUCTOR user, create their profile via `POST /instructors/create` with `userId` set to the new `accounts_user.id`.
5. **EDT progress** is student-scoped using `accounts_user.id` as `studentId` — replace any Django FK references accordingly.
6. **Profile pictures** are now uploaded and served by this API (`POST /instructors/{id}/profile-picture`, served back under `/uploads/...`) — stop reading/writing Django's `MEDIA_ROOT` for instructor photos.
7. **New instructor sign-ups default to `approvalStatus: "pending"`** — an ADMIN must call `PATCH /instructors/{id}/approval` before the instructor should be shown as bookable in the Django client.
