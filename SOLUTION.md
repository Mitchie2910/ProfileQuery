# SOLUTION

## OPTIMIZATIONS

1. **Query Performance**
- Indexing: In order to improve Database hit queries, I indexed the filter columns that would be more likely get queried. These are the name, gender, age_group, country_name/country_id. The text data.
- Caching: Results from the GET /api/profiles and GET /api/profiles/id are cached for fast, in-memory lookup
- Connection pooling: Configured and tuned connection pooling to 9 connection pools constantly open to serve Database operations.

2. **Query Normalization**
- Already, my search query was normalized from the previous task. But I did not implement the nationality to country id mapping. I did that in this stage. \
To handle that, I sourced a file containing iso country codes and their corresponding denonyms(nationalities). I built a map which mapped the iso codes to the denonyms. The tokenized search is then passed through this map to possibly extract country id fom nationality.
- To handle different forms of expressing numerical filters like max age, min age, etc, REGEX parsing was used.

3. **CSV Data Ingestion**
- A buffered reader is used to a batch of lines from the request to the processing service. The buffered reader reads a chunk of the file, not everything into the memory. \
- A validator service is then used to check each read record for null entries and invalid data entries. The method returns the returns an Optional<?> type to be persisted to the database. This means that if a record is deemed fault, the validator can record the invalid entries and return Optional.empty. \
While valid entries are batched using Optional.get().
- When the batched data reaches a suitable size of 1000 records as used in the implementation, That batch is then written to the database. The batch is cleared and the cycle repeats.

## INGESTION FAILURES
- Ingestion failures are handled in a way such that the user always receives useful feedback. If the file cannot be readback, a http error is thrown containing the failure details.
- As long as the file can be read, useful results would be shown to the user, including skipped lines, insert lines, total lines, failure causes and the number of occurrences.


## BEFORE AND AFTER COMPARISON
I genuinely do not have these comparisons because I made most of these optimizations before the stage 4 tasks. I believe my commit history can prove this.