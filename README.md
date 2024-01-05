# Speer Assignment For Backend 
## (This repository contains the backend code for the Assignment for position of Backend Developer)

# Tech Stack
This project uses the following tech stack:
* Spring Boot
* Hibernate
* MySQL (For Database)
* Elasticsearch (For Indexing)

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

## Get Note by ID: GET /api/notes/:id
* Retrieve a specific note by ID for the authenticated user.

## Create New Note: POST /api/notes
* Create a new note for the authenticated user.

## Update Note by ID: PUT /api/notes/:id
* Update an existing note by ID for the authenticated user.

##  Delete Note by ID: DELETE /api/notes/:id
* Delete a note by ID for the authenticated user.

## Share Note: POST /api/notes/:id/share
* Share a note with another user for the authenticated user.

## Search Notes: GET /api/search?q=:query
* Search for notes based on keywords for the authenticated user.


You are welcome to explore these endpoints to leverage the full potential of the application






