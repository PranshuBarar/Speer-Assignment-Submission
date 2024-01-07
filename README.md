# Speer Assignment For Backend
This repository serves as the backend implementation for the Backend Developer position assignment. The project features a range of functionalities aimed at delivering a robust and efficient backend system.

## Features of this Application
**Secure Endpoints:** <br> *Utilize Spring Security for robust endpoint security, protecting against unauthorized access.*

**JWT Authentication:** <br> *Implement JWT-based authentication management for secure user access.*

**Rate Limiting (Bucket4j):** <br> *Ensure optimal system performance with API rate limiting using Bucket4j, preventing abuse.*

**Elasticsearch Indexing:** <br> *Optimize data retrieval with Elasticsearch indexing, enhancing search capabilities.*

## Table of Contents
- [Instructions for Running this Project on your Computer](#instructions-for-running-this-project-on-your-computer)
   * [Prerequisites](#prerequisites)
   * [How to Run](#how-to-run)
   * [Additional Notes](#additional-notes)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
   * [Application Structure](#application-structure)
   * [MySQL Database Structure](#mysql-database-structure)
- [API Documentation](#api-documentation)
   * [Overview](#overview)
   * [Authentication Endpoints](#authentication-endpoints)
      + [Sign Up: POST /api/auth/signup](#sign-up-post-apiauthsignup)
      + [Login: POST /api/auth/login](#login-post-apiauthlogin)
   * [Note Endpoints](#note-endpoints)
      + [Get All Notes: GET /api/notes](#get-all-notes-get-apinotes)
      + [Get Note by ID: GET /api/notes/{noteId}](#get-note-by-id-get-apinotesnoteid)
      + [Create New Note: POST /api/notes](#create-new-note-post-apinotes)
      + [Update Note by ID: PUT /api/notes/{noteId}](#update-note-by-id-put-apinotesnoteid)
      + [Delete Note by ID: DELETE /api/notes/{noteId}](#delete-note-by-id-delete-apinotesnoteid)
      + [Share Note: POST /api/notes/{noteId}/share](#share-note-post-apinotesnoteidshare)
      + [Search Notes: GET /api/search?q=query](#search-notes-get-apisearchqquery)

_________________________________________________
## Instructions for Running this Project on your Computer
_________________________________________________

## Prerequisites

Make sure you have the following tools installed on your machine:

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)
- [JDK 17 or higher](https://www.oracle.com/java/technologies/downloads/#java17)

## How to Run

1. Clone the repository to your local machine:

    ```bash
    git clone https://github.com/PranshuBarar/Speer-Assignment-Submission.git
    ```

2. Navigate to the project directory:

    ```bash
    cd <respository_name_here>
    ```

3. Run Docker Containers of MySQL and Elasticsearch:

    ```bash
    docker compose up
    ```

4. Now run the following commands and start your Spring Boot application:

    ```bash
    mvn clean install
    ```
   ```bash
    mvn spring-boot:run
    ```

5. Access the application at [http://localhost:8080/springboot3/swagger-ui/index.html#/](http://localhost:8080/springboot3/swagger-ui/index.html#/)

## Additional Notes

- If you encounter any issues, ensure that ports 8080, 3306, and 9200 are available on your machine.

- Customize your application configuration in the respective `application.properties` or `application.yml` files if you require doing so.

- You are most welcome to explore other available endpoints and functionalities as documented in the project.

_________________________________________________
# Tech Stack
_________________________________________________
This project uses the following tech stack:
* [Spring Boot](https://spring.io/projects/spring-boot)
* [Hibernate](https://hibernate.org/)
* [MySQL](https://www.mysql.com/) (For Database)
* [Elasticsearch](https://www.elastic.co/) (For Indexing)
* [OpenAPI (for API Documentation)](https://www.openapis.org/)

_________________________________________________
# Project Structure
_________________________________________________

## Application Structure
![imgonline-com-ua-twotoone-aaoLcGP81DFMhJb](https://github.com/PranshuBarar/Speer-Assignment-Submission/assets/117909106/332fe99b-f2db-45b2-99db-78d268d07205)

## MySQL Database Structure

_________________________________________________
# API Documentation
_________________________________________________

## Overview
* I've implemented a set of RESTful endpoints that facilitate various functionalities in the application.

# Authentication Endpoints

## Sign Up: [POST /api/auth/signup](#)
* Create a new user account.

## Login: [POST /api/auth/login](#)
* Log in to an existing user account and receive an access token.

# Note Endpoints

## Get All Notes: [GET /api/notes](#)
* Retrieve a list of all notes for the authenticated user.

## Get Note by ID: [GET /api/notes/{noteId}](#)
* Retrieve a specific note by ID for the authenticated user.

## Create New Note: [POST /api/notes](#)
* Create a new note for the authenticated user.

## Update Note by ID: [PUT /api/notes/{noteId}](#)
* Update an existing note by ID for the authenticated user.

## Delete Note by ID: [DELETE /api/notes/{noteId}](#)
* Delete a note by ID for the authenticated user.

## Share Note: [POST /api/notes/{noteId}/share](#)
* Share a note with another user for the authenticated user.

## Search Notes: [GET /api/search?q=query](#)
* Search for notes based on keywords for the authenticated user.

You are welcome to explore these endpoints to leverage the full potential of the application
