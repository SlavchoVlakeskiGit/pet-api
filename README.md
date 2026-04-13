# Pet API

A REST API for managing pets, built with Java 17 and Spring Boot.

## How to run

```bash
.\mvnw.cmd spring-boot:run
```

> Make sure you are in the project root directory before running this command.

The API will start on `http://localhost:8080`.

## Endpoints

| Method | Endpoint     | Description      |
|--------|--------------|------------------|
| GET    | /pets        | Get all pets     |
| GET    | /pets/{id}   | Get a pet by id  |
| POST   | /pets        | Create a new pet |
| PATCH  | /pets/{id}   | Update a pet     |
| DELETE | /pets/{id}   | Delete a pet     |

## Running tests

```bash
.\mvnw.cmd test
```

## Example request

```json
POST /pets
{
  "name": "Milo",
  "species": "Dog",
  "age": 3,
  "ownerName": "Jane"
}
```

## Design notes

- Layered architecture: Controller, Service, Repository
- Persistence is mocked in-memory as permitted by the assignment. The repository interface is designed to make a future migration to a relational database like MySQL straightforward — swapping the implementation requires no changes to the service layer.
- PATCH semantics for updates — only the fields included in the request are changed, everything else stays as-is.
- Validation is split: structural rules (required fields, value constraints) live in the DTOs, business rules (blank string rejection on update) live in the service.
- Create and Update use separate request DTOs because their validation rules are different.
- Chose not to use @Entity to keep the domain model free from persistence concerns.
