## Data Persistence and API Integration

### Overview

Backend service that enriches a name using external APIs, stores processed data, and exposes REST endpoints for retrieval and management.

### External APIs

- <https://api.genderize.io?name={name}>
- <https://api.agify.io?name={name}>
- <https://api.nationalize.io?name={name}>

### Data Processing

Genderize → gender, gender_probability, count → sample_size
- Agify → age + age_group
0–12 child, 13–19 teenager, 20–59 adult, 60+ senior
- Nationalize → highest probability country → country_id + country_probability

## EndPoints

### POST api/profiles

Creates Profile from name

Request:

```json
{ "name": "ella" }
```

Success(201)

```json
{
  "status": "success",
  "data": {
    "id": "uuid-v7",
    "name": "ella",
    "gender": "female",
    "gender_probability": 0.99,
    "sample_size": 1234,
    "age": 46,
    "age_group": "adult",
    "country_id": "DRC",
    "country_probability": 0.85,
    "created_at": "2026-04-01T12:00:00Z"
  }
}
```

Idempotent Response(200)

```json
{
  "status": "success",
  "message": "Profile already exists",
  "data": {}
}
```

### GET api/profiles{id}

```json
{
  "status": "success",
  "data": {}
}
```

### GET api/profiles{id}

Filters `gender`, `country_id` and `age_group`

```json
{
  "status": "success",
  "count": 2,
  "data": []
}
```

### GET api/profiles{id}

204 No Content

## Errors

### Validation

- 400 -> missing/empty name
- 422 -> invalid input

### Not Found

- 404 -> profile not found

### Error Structure

```json
{
  "status": "error",
  "message": "error message"
}
```
