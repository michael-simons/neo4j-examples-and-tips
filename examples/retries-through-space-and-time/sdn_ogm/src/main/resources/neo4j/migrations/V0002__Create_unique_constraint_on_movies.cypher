CREATE CONSTRAINT movies_unique_title
ON (m:Movie)
ASSERT m.title IS UNIQUE;
