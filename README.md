# Plebicom Technical Test

Technical test project using Java 11, Spring Boot, Spring Security, JWT, and JPA. A base code is provided containing the foundation of a simple project. The goal of this technical test is to improve it along the following axes:
* Code quality and best practices
* Fixing functional errors
* Tests and tools (Postman, unit tests)

## Endpoints

- `POST /auth/register`: User registration
- `POST /auth/login`: Authentication (returns a JWT)
- `GET /tasks`: Retrieves the user's tasks
- `POST /tasks`: Creates a task
- `DELETE /tasks/{id}`: Deletes a task
- `GET /admin/users`: Lists all users (admin only)

## Expected Deliverables

To validate this test, clone this repo into your own Git repository, and validate each point below through a commit or a pull request including explanations of your code and your answers to the questions.

## Launch

```bash
mvn spring-boot:run
```

# Test Plan

Throughout your test, special attention must be given to code quality and adherence to common best practices when programming in Java/Spring. Any improvement to the existing code can be a plus in the evaluation of your application.

1. Compile and run the program.<br/>
   The test is designed so that the program compiles but does not start. Understand why, fix the program so that it runs without losing any functionality. Explain in your commit comment why it wasn't starting.
2. Fix the `/tasks/{id}` endpoint<br/>
   While starting a Postman collection, fix the endpoint that allows task creation or modification. <br/>
     * As it stands, the project automatiqualy update the descriptions with a prefix string any time. It shall be added only if user has "ADMIN" role. Find out why, fix it, and provide explanations in your commit.
     * As it stands, the project endpoint return an infinite loop of Task / User object. Find a way to have a correct output.
3. Add an elevated right to validate a task<br/>
   Modify the code so that only administrators can set `done=true` for a task.
4. Create a Dockerfile suitable for production, for which the resulting image is as small as possible and with minimal privileges.
5. Create a Postman Collection to test the application endpoints
5. Add JUnit unit tests covering the application's services and controllers.
