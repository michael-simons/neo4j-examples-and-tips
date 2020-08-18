CREATE CONSTRAINT people_unique_name
ON (p:Person)
ASSERT p.name IS UNIQUE;
