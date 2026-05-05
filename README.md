## Insighta Labs - Profile Query API
This is a backend system for looking up, storing and querying demographic profile data.
It supports extensive filtering, sorting, pagination and rule-based natural language querying.

## Features
- Multi API integration and processing
- Combined filtering by multiple fields 
- Sorting 
- Pagination 
- Natural Language Query Parsing 

## Tech Stack
- Java (Spring Boot)
- PostgreSQL
- Maven

## API Endpoints

### GET /api/profiles
Supports filtering, sorting and pagination
#### Query parameters
- gender
- age_group
- country_id
- min_age
- max_age
- min_gender_probability
- min_country_probability
- order (asc, desc)
- page (default: 1)
- limit (default: 10, max: 50)
- sort_by(age, created_at, gender_probability)

#### Response Format
```json
{
  "status": "success",
  "page": 1,
  "limit": 10,
  "total": 2026,
  "data": [
    {
      "id": "019db1f4-bac6-70e4-acf2-d1cb3352440a",
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
  ]
}
```

### GET /api/profiles/search?q=

Parses plain English queries into structured filters

#### Response Format
```json
{
  "status": "success",
  "page": 1,
  "limit": 10,
  "total": 2026,
  "data": [
    {
      "id": "019db1f4-bac6-70e4-acf2-d1cb3352440a",
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
  ]
}
```

### GET api/profiles{id}
#### Response format

```json
{
  "status": "success",
  "data": {
    "id": "019db1f4-bac6-70e4-acf2-d1cb3352440a",
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

### GET api/profiles{id}

204 No Content


### POST /api/profiles

Creates Profile from name

Request:

```json
{ "name": "ella" }
```
#### Response Format
Success(201)

```json
{
  "status": "success",
  "data": {
    "id": "019db1f4-bac6-70e4-acf2-d1cb3352440a",
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
  "data": {
    "id": "019db1f4-bac6-70e4-acf2-d1cb3352440a",
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

## Natural Language Parsing Approach
The system uses a rule-based parser to extract structured queries from user queries

### Architecture

- The rules and extraction methods are defined first in a class. There are two types of rules defined.
- The first type of rule extracts discrete parameters from the query. This was implemented by mapping possible synonyms of certain query parameters to a structured parameter. For example, the words "boy", "boys", "men", "man" were all mapped to the parameter "male" which can then be used to correctly query the database. This rule class was used to extract gender, age_group and country_id parameters.
- The second type of rule extracts continuous numerical data defined by thresholds. This was implemented by using regex parsing to identify clusters of (parameter name) + (threshold) which appear in the query string. For example, the phrase "gender probability above 0.75", the regex rule would identify the keywords gender, probability, above and the accompanying float. This rule class was used to extract min_age, max_age, min_country_probability and min_gender_probability.
- Each rule has it's corresponding extration method. The extraction method for the discrete rule would take in a list of words which are from the tokenized query sentence so as to allow for iteration and comparison with values stored in the maps.
- The extraction methods for the continuous numerical rule take in the normalized query sentence (lowercase and trim).

### Steps
1. Upon entry to the processing orchestrator, the query sentence is normalized (lowercased and trailing white spaces trimmed).
2. The normalized text is then tokenized to a list of words contained in the sentence.
2. An empty query parameter object containing null fields of structured query parameters is instantiated.
3. The query parameter object is then passed along with the sentence or token, depending on the extraction method being targeted, into the all extraction methods sequentially.
4. This causes the query parameter object to be mutated up until the last extraction method.
5. The resulting query parameter object would contain the structured query and is then passed to the orchestrator caller.

### Supported Keywords
| Phrase                                                                                         | Mapping                             |
|------------------------------------------------------------------------------------------------|-------------------------------------|
| male / males / boy / man / men / gentleman / boys / guy / guys                                 | gender = male                       |
| female / females / girl / girls / lady / ladies / woman / women                                | gender = female                     |
| child / children / kid / kids / toddler / infant                                               | age_group = children                |
| teens / teenager / adolescent / teen                                                           | age_group = teenager                |
| adult / adults / grown up / grown-ups / grown                                                  | age_group = adult                   |
| senior / seniors / elderly / retired                                                           | age_group = senior                  |
| ISO country display name                                                                       | country_id = ISO country code       |
| young /                                                                                        | min_age = 16, max_age = 24          |
| gender + ( probability / confidence / score ) + threshold                                      | min_gender_probability = threshold  |
| country + ( probability / confidence / score ) + threshold                                     | min_country_probability = threshold |
| ( minimum / min ) + country + ( probability / confidence / score ) + of (optional) + threshold | min_country_probability = threshold |
| ( minimum / min ) + gender + ( probability / confidence / score ) + of (optional) + threshold  | min_gender_probability = threshold  |
| (above / over /greater than /more than /older than /over the age of) + threshold               | min_age = threshold                 |
| (below / under /lesser than /less than /younger than /under the age of) + limit                | max_age = limit                     |

### Examples

"young males from nigeria"  
gender=male, min_age=16, max_age=24, country_id=NG

"females above 30"  
gender=female, min_age=30

"adult males from kenya"  
gender=male, age_group=adult, country_id=KE

### Limitations

- This solution does not handle complex sentence structures such as negation.
- This solution forces limited vocabulary when querying.
- When querying for country names the exact ISO country name has to be used, there is no allowance for slangs and denonyms.
- There is no support for synonyms beyond the defined keywords.



## How to Run
### 1. Clone the repo
`git clone https://github.com/Mitchie2910/ProfileQuery.git` \
`cd profilequery`

### 2. Set up the PostgreSQL database

Create your database and configure environment variales as such:

`DATABASE_URL` = your database URL \
`DB_USERNAME` = your username \
`DB_PASSWORD` = your user password

Initilaize the database

### 3. Build the project
`mvn clean package -DskipTests`

### 4. Run the application
`java target/nameprocessing-0.0.1-SNAPSHOT.jar`

## ENVIRONMENT KEYS
`DATABASE_URL`
`DB_PASSWORD`
`DB_USERNAME`
`CLIENT_ID`
`CLIENT_SECRET`
`REDIS_PORT`
`REDIS_HOST`
`FRONTEND_URL`






