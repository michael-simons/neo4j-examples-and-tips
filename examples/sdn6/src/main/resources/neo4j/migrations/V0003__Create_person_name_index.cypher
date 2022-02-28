CREATE INDEX person_name_index IF NOT EXISTS FOR (n:Person) ON (n.name);
