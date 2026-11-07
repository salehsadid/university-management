# University Management System API

A Spring Boot REST API with three roles: **Admin**, **Teacher** and **Student**.

- Java 17, Spring Boot, Spring Data JPA, PostgreSQL 17
- Spring Security: session login (JSON endpoint), BCrypt password hashing, CSRF protection, role-based access control
- Docker Compose runs the app and PostgreSQL together

## Running

### With Docker Compose (recommended)

```bash
docker compose up --build
```

This starts two containers:

| Service | What it is | Port |
|---------|------------|------|
| `db`    | PostgreSQL 17, database `university_db`, data kept in the `pg-data` volume | not published |
| `app`   | The Spring Boot API, starts once `db` is healthy | `8080` |

Open `http://localhost:8080` in a browser for the web frontend. The API is under `http://localhost:8080/api/...`.
Stop it with `docker compose down` (add `-v` to also delete the database volume).

### Locally (without Docker for the app)

Start a PostgreSQL server on `localhost:5432` with database `university_db` and user `postgres` / `postgres`, then:

```bash
./mvnw spring-boot:run
```

## Default admin

On startup the app creates the first admin if no admin exists yet:

| Email           | Password   |
|-----------------|------------|
| `admin@uni.edu` | `admin123` |

Change these with the properties `app.admin.name`, `app.admin.email` and `app.admin.password`
(or the environment variables `APP_ADMIN_NAME`, `APP_ADMIN_EMAIL` and `APP_ADMIN_PASSWORD`).

## Web frontend

A small frontend (plain HTML, CSS and JavaScript in `src/main/resources/static/`) is served by the app itself
at `http://localhost:8080`. It uses the same API and the same login flow as Postman.

| Logged in as | What you see |
|--------------|--------------|
| ADMIN        | Tabs for Students, Teachers and Admins: a list of each, with **+ Add**, **Edit** and **Delete** |
| STUDENT      | Own profile, with **Edit Profile** (name, email, password, phone) and **Delete Account** |
| TEACHER      | Same as student, for the teacher profile |

To try it: log in as the default admin, add a student and a teacher, log out, then log in with the
student's or teacher's email and password.

## Login flow

The API uses a session cookie (`JSESSIONID`) and CSRF protection. Every `POST`, `PUT` and `DELETE`
request, **including login and logout**, must send the CSRF token in the `X-CSRF-TOKEN` header.

1. **Get a CSRF token**: `GET /api/auth/csrf` returns `{"headerName": "X-CSRF-TOKEN", "token": "..."}`.
2. **Log in**: `POST /api/auth/login` with body `{"email": "...", "password": "..."}` and the `X-CSRF-TOKEN` header.
   Returns `{"email": "...", "role": "ADMIN"}` and sets the session cookie.
3. **Call the API** with the session cookie (and the CSRF header for `POST`/`PUT`/`DELETE`).
4. **Log out**: `POST /api/auth/logout` with the CSRF header. Returns `204`.

After logging out, get a new CSRF token before logging in again.

Example with curl:

```bash
# 1. CSRF token (stored in the session, so keep the cookie jar)
TOKEN=$(curl -s -c cookies.txt -b cookies.txt http://localhost:8080/api/auth/csrf | sed -E 's/.*"token":"([^"]+)".*/\1/')

# 2. Login
curl -c cookies.txt -b cookies.txt -H "Content-Type: application/json" -H "X-CSRF-TOKEN: $TOKEN" \
     -X POST http://localhost:8080/api/auth/login -d '{"email":"admin@uni.edu","password":"admin123"}'

# 3. Call the API
curl -b cookies.txt http://localhost:8080/api/students

# 4. Logout
curl -c cookies.txt -b cookies.txt -H "X-CSRF-TOKEN: $TOKEN" -X POST http://localhost:8080/api/auth/logout
```

## Role-based access control

| Path                                                  | Who can access |
|-------------------------------------------------------|----------------|
| `GET /api/auth/csrf`, `POST /api/auth/login`          | Everyone       |
| Frontend files (`/`, `/index.html`, `/app.js`, `/style.css`) | Everyone |
| `/api/students/me`                                    | STUDENT        |
| `/api/teachers/me`                                    | TEACHER        |
| `/api/students/**`, `/api/teachers/**`, `/api/admins/**` | ADMIN       |
| Everything else (e.g. `/api/auth/me`, `/api/auth/logout`, `/api/hello`) | Any logged-in user |

- Not logged in: **401 Unauthorized**
- Logged in but not allowed, or missing/invalid CSRF token: **403 Forbidden**

Students and teachers can only see and change **their own** profile through the `/me` endpoints.
Only `name`, `email`, `password` and `phone` can be changed there. Changing the email or the password,
or deleting the profile, ends the session, so the user has to log in again.

## Endpoints

Errors are returned as [Problem Details](https://www.rfc-editor.org/rfc/rfc9457) JSON
(`title`, `status`, `detail`; validation errors also include an `errors` map).

### Auth

| Method | Path               | Success | Other status codes |
|--------|--------------------|---------|--------------------|
| GET    | `/api/auth/csrf`   | 200     |                    |
| POST   | `/api/auth/login`  | 200     | 400 invalid body, 401 wrong email or password, 403 missing CSRF token |
| GET    | `/api/auth/me`     | 200     | 401                |
| POST   | `/api/auth/logout` | 204     | 403 missing CSRF token |

### Admins (ADMIN)

| Method | Path               | Success | Other status codes |
|--------|--------------------|---------|--------------------|
| GET    | `/api/admins`      | 200     | 401, 403           |
| GET    | `/api/admins/{id}` | 200     | 401, 403, 404      |
| POST   | `/api/admins`      | 201 + `Location` header | 400, 401, 403, 409 email in use |
| PUT    | `/api/admins/{id}` | 200     | 400, 401, 403, 404, 409 |
| DELETE | `/api/admins/{id}` | 204     | 401, 403, 404      |

### Teachers

| Method | Path                 | Role    | Success | Other status codes |
|--------|----------------------|---------|---------|--------------------|
| GET    | `/api/teachers`      | ADMIN   | 200     | 401, 403           |
| GET    | `/api/teachers/{id}` | ADMIN   | 200     | 401, 403, 404      |
| POST   | `/api/teachers`      | ADMIN   | 201 + `Location` header | 400, 401, 403, 409 |
| PUT    | `/api/teachers/{id}` | ADMIN   | 200     | 400, 401, 403, 404, 409 |
| DELETE | `/api/teachers/{id}` | ADMIN   | 204     | 401, 403, 404      |
| GET    | `/api/teachers/me`   | TEACHER | 200     | 401, 403           |
| PUT    | `/api/teachers/me`   | TEACHER | 200     | 400, 401, 403, 409 |
| DELETE | `/api/teachers/me`   | TEACHER | 204     | 401, 403           |

### Students

| Method | Path                 | Role    | Success | Other status codes |
|--------|----------------------|---------|---------|--------------------|
| GET    | `/api/students`      | ADMIN   | 200     | 401, 403           |
| GET    | `/api/students/{id}` | ADMIN   | 200     | 401, 403, 404      |
| POST   | `/api/students`      | ADMIN   | 201 + `Location` header | 400, 401, 403, 409 email or student ID in use |
| PUT    | `/api/students/{id}` | ADMIN   | 200     | 400, 401, 403, 404, 409 |
| DELETE | `/api/students/{id}` | ADMIN   | 204     | 401, 403, 404      |
| GET    | `/api/students/me`   | STUDENT | 200     | 401, 403           |
| PUT    | `/api/students/me`   | STUDENT | 200     | 400, 401, 403, 409 |
| DELETE | `/api/students/me`   | STUDENT | 204     | 401, 403           |

### Example request bodies

Create student (`POST /api/students`):

```json
{
  "studentId": "CSE-2021-001",
  "name": "Rahim Uddin",
  "email": "rahim@uni.edu",
  "password": "student123",
  "department": "CSE",
  "semester": "3-2",
  "phone": "+8801712345678"
}
```

Update own profile (`PUT /api/students/me` or `PUT /api/teachers/me`; `password` and `phone` are optional):

```json
{
  "name": "Rahim Uddin",
  "email": "rahim@uni.edu",
  "password": "newpass123",
  "phone": "+8801798765432"
}
```

## Postman collection

`University-API.postman_collection.json` in the project root contains a request for every endpoint.

1. In Postman choose **Import** and select `University-API.postman_collection.json`.
2. Run **Auth > Get CSRF**. It saves the token into the `csrfToken` collection variable.
3. Run **Auth > Login (Admin)**. Postman keeps the session cookie automatically.
4. Call any other request. A collection pre-request script adds the `X-CSRF-TOKEN` header to every request.

Run **Get CSRF** again after **Logout**, or after changing your own email or password.
The `baseUrl` collection variable defaults to `http://localhost:8080`.
