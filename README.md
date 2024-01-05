# Speer Assignment For Backend
## (This repository contains the backend code for the Assignment for position of Backend Developer)

_________________________________________________
Instructions for Running this Project on your Computer
_________________________________________________

## Prerequisites

Make sure you have the following tools installed on your machine:

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

## How to Run

1. Clone the repository to your local machine:

    ```bash
    git clone https://github.com/PranshuBarar/Speer-Assignment-Submission.git
    ```

2. Navigate to the project directory:

    ```bash
    cd <respository_name_here>
    ```

3. Build the Docker image for your Spring Boot application:

    ```bash
    docker build -t speer-application-backend .
    ```

4. Run Docker Compose to start MySQL, Elasticsearch, and your Spring Boot application:

    ```bash
    docker-compose up
    ```

5. Access your application at [http://localhost:8080](http://localhost:8080)

## Additional Notes

- If you encounter any issues, ensure that ports 8080, 3306, and 9200 are available on your machine.

- Customize your application configuration in the respective `application.properties` or `application.yml` files if you require to do so.

- You are most welcome to explore other available endpoints and functionalities as documented in the project.


# Tech Stack
This project uses the following tech stack:
* Spring Boot
* Hibernate
* MySQL (For Database)
* Elasticsearch (For Indexing)

# Project Structure

## Application Structure
![imgonline-com-ua-twotoone-HTWW9MlcgzYnpss](https://github.com/PranshuBarar/Speer-Assignment-Submission/assets/117909106/23a64320-22ca-46b8-a94a-7ab865fe5117)

## MySQL Database Structure


# API Documentation
## Overview
* I've implemented a set of RESTful endpoints that facilitate various functionalities in the application.

# Authentication Endpoints

## Sign Up: POST /api/auth/signup
* Create a new user account.

## Login: POST /api/auth/login
* Log in to an existing user account and receive an access token.

# Note Endpoints

## Get All Notes: GET /api/notes
* Retrieve a list of all notes for the authenticated user.

## Get Note by ID: GET /api/notes/{noteId}
* Retrieve a specific note by ID for the authenticated user.

## Create New Note: POST /api/notes
* Create a new note for the authenticated user.

## Update Note by ID: PUT /api/notes/{noteId}
* Update an existing note by ID for the authenticated user.

##  Delete Note by ID: DELETE /api/notes/{noteId}
* Delete a note by ID for the authenticated user.

## Share Note: POST /api/notes/{noteId}/share
* Share a note with another user for the authenticated user.

## Search Notes: GET /api/search?q=query
* Search for notes based on keywords for the authenticated user.


You are welcome to explore these endpoints to leverage the full potential of the application






