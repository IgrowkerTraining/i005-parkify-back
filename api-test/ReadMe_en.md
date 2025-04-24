# README: API Tests for Parkify Backend (test_api.sh)

## Purpose

This document describes how to run and interpret the results of the automated API tests for the Parkify backend, using the provided shell script `test_api.sh`. The script executes a sequence of `curl` requests against the locally running Parkify application (via Docker Compose) to verify the core MVP functionality and handle some negative scenarios.

**Even if you don't run the script**, the **"Test Coverage"** section below provides a clear overview of which endpoints and user scenarios are verified by this test. This will help you assess the backend's readiness and, if needed, manually replicate specific requests (e.g., in Postman).

## Prerequisites

Before running the tests, ensure the following components are installed and configured on your system:

1.  **Docker and Docker Compose:** Required to run the backend application and PostgreSQL database in containers. Make sure the Docker daemon (or Docker Desktop) is running.
2.  **Command Line Shell:**
    *   **Linux/macOS:** The standard `Terminal`.
    *   **Windows:** **Git Bash** (installed with Git) or **WSL** (Windows Subsystem for Linux) is recommended. The standard `cmd` or `PowerShell` might not fully support the script's syntax.
3.  **`curl`:** A command-line utility for making HTTP requests. Usually pre-installed on Linux/macOS and included with Git Bash.
4.  **`jq`:** A command-line utility for processing JSON. Used in the script to extract the token and format output. **Important:** Without `jq`, the script might not work correctly, or its output will be less readable.
    *   **Check Installation:** Open your terminal/Git Bash and type `jq --version`. If you see a version number, `jq` is installed. If not, install it.
    *   **Installing `jq`:**
        *   Debian/Ubuntu: `sudo apt update && sudo apt install jq`
        *   Fedora/CentOS: `sudo yum install jq` or `sudo dnf install jq`
        *   macOS (Homebrew): `brew install jq`
        *   Windows (via Git Bash): You can download the `jq.exe` executable from the [official website](https://jqlang.github.io/jq/download/) and place it in a directory included in your `PATH` (e.g., the `bin` directory within your Git installation), or simply place `jq.exe` in the same folder as `test_api.sh`.
        *   Windows (Chocolatey): `choco install jq`

## Environment Setup and Startup

1.  **Navigate to the root directory of the Parkify Backend project**, where the `docker-compose.yml` (and `Dockerfile`) file is located.
2.  **Ensure Docker Desktop (or the Docker daemon) is running.**
3.  **Start the application using Docker Compose:**
    *   Open a terminal (or Git Bash) **in this root directory**.
    *   Execute the command:
        ```bash
        docker-compose up -d --build
        ```
    *   `--build`: Rebuilds the Docker image. Important for the first run or after changes to Java/Spring code or the `Dockerfile`.
    *   `-d`: Runs containers in detached mode (in the background).
4.  **Wait for the Spring Boot application to fully start.** The application isn't ready to accept requests immediately. You need to monitor the logs for the startup confirmation.
    *   Execute the command to view the logs of the `spring_boot_app` container:
        ```bash
        docker-compose logs -f spring_boot_app
        ```
    *   Testing can begin **only after** a line similar to this appears (startup time may vary):
        ```log
        spring_boot_app  | .... INFO ... --- [mini-project] [           main] c.i.feature.parkify.ParkifyApplication   : Started ParkifyApplication in ... seconds ...
        ```
    *   Once you see this line, press `Ctrl+C` to stop following the logs (the containers will continue running in the background).

## Running the Tests

1.  **Navigate to the directory** where the `test_api.sh` script file is located.
2.  **Make the script executable (only needs to be done once):**
    *   Linux, macOS, and Git Bash require explicit permission to run files as programs.
    *   Open a terminal (or Git Bash) **in the directory containing `test_api.sh`**.
    *   Execute the `chmod +x` command (change mode, add executable permission):
        ```bash
        chmod +x test_api.sh
        ```
    *   You only need to run this command **once** for this file. Afterward, the system will know it can be executed.
3.  **Run the script:**
    *   While in the same directory, execute:
        ```bash
        ./test_api.sh
        ```
    *   The `./` characters tell the system to run the file from the current directory.
    *   **Viewing and Saving Output:** The output will be displayed in the console. If the output is long or you want to save it for analysis, use output redirection to a file:
        ```bash
        ./test_api.sh | tee test_output.log
        ```
        This command will show the output on the screen *and* save it to the file `test_output.log`.

## Interpreting Results

The script outputs information about the steps being executed and their results:

*   **`[OK] Step Name: Expected status XXX received.`**: Indicates the test step completed successfully, and the API returned the expected HTTP status.
*   **`[ERROR] Step Name: Expected status XXX, received 'YYY'.`**: Indicates the test step failed. The API returned an unexpected HTTP status. The script will also print the response body on error to aid diagnostics.
*   **`[INFO]`**: Informational messages.
*   **`--- Testing Completed ---`**: Message indicating the script has finished execution.

Review the entire output for any `[ERROR]` messages. If there are no errors, the API tests are considered passed.

## Test Coverage

The `test_api.sh` script verifies the following core scenarios and endpoints:

*(This section provides a quick overview of what is tested, even without running the script)*

1.  **Owner Authentication & Registration (`/api/v1/auth`)**
    *   `POST /register`: Successful registration, error on duplicate registration (409), validation errors (invalid email, short password - 400).
    *   `POST /login`: Successful login, JWT token retrieval, error on wrong password (401), error on non-existent user (401), validation error (blank password - 400).
2.  **Owner Parking Management (`/api/v1/parkings`)**
    *   `POST /my`: Parking creation (success 201), security errors (no token/invalid token - 403), validation error (negative capacity - 400).
    *   `PATCH /{id}/availability`: Availability update (success 200), security errors (no token - 403), business logic errors (updating non-existent parking - 404; updating with invalid values [negative, > capacity] - 400).
    *   `GET /my-list`: Retrieving the owner's parking list (success 200).
    *   `DELETE /my`: Owner deleting their parking (success 204), checking access after deletion (404).
3.  **Public Parking Endpoints (`/api/v1/parkings`)**
    *   `GET /availability?ids=...`: **Batch Availability Request.** Allows fetching the current number of available spots for **multiple parkings simultaneously** in a single request. This is crucial for the Frontend, e.g., for updating information on map markers or in a user's "Favorites" list without making numerous individual requests. The test checks: successful data retrieval, reflection of updates made by the owner, handling of non-existent IDs in the list, handling of an empty ID list (error 400).
    *   `GET ?latitude=...&longitude=...`: Nearby parking search (success 200), filter checks (`radius`, `maxPrice`, `minAvailability`), pagination checks (`limit`, `offset`), errors on missing required parameters `latitude`/`longitude` (400).
    *   `GET /{id}`: Get parking details (success 200), error on non-existent ID (404).
    *   `GET /{id}/availability`: Get single parking availability (success 200), error on non-existent ID (404).
4.  **Content and Configuration (`/api/v1/content`, `/api/v1/config`)**
    *   `GET /content/footer`: Successful data retrieval (200).
    *   `GET /content/home`: Successful data retrieval (200).
    *   `GET /config/initial`: Successful data retrieval (200).
5.  **Security (General Checks)**
    *   Checking access to protected endpoints (`/my-list`, `PATCH /{id}/availability`, `DELETE /my`) without a token (expects 403).
6.  **Cleanup**
    *   Checking access to deleted resources (expects 404).

## Troubleshooting

*   **Error "Connection refused":** Ensure the Docker Compose stack is running (`docker-compose ps`) and the `spring_boot_app` container is running and accessible at `localhost:8080`. Check if port 8080 is occupied by another process.
*   **Application fails to start:** Check the logs of the `spring_boot_app` container (`docker-compose logs -f spring_boot_app`) for errors during Spring Boot startup or database connection.
*   **Error "jq: command not found":** Install `jq` according to the instructions in the "Prerequisites" section.
*   **Error "Permission denied" when running `./test_api.sh`:** You haven't granted execute permissions to the script. Refer to "Running the Tests", step 2, and run `chmod +x test_api.sh`.
*   **Error "./test_api.sh: No such file or directory":** You are not in the directory where the script is located. Use the `cd <path_to_script_folder>` command to navigate to the correct directory.
*   **Tests fail unexpectedly:** Check the script output and the response body at the failing step. The API logic might have changed, there might be a backend bug, or the application/DB didn't start correctly.

## Notes and Limitations

*   The tests assume they are run against a "clean" database (as created by `docker-compose up`). Subsequent runs of the script use unique emails for registration to avoid conflicts.
*   These tests **do not measure API performance**.
*   The tests cover the main MVP scenarios but **do not guarantee** verification of all possible edge cases or parameter combinations.
*   The script depends on the current API structure. Changes to paths, parameters, or response formats may require updating the script.