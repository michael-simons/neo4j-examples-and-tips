CREATE INDEX movie_title_index IF NOT EXISTS FOR (n:Movie) ON (n.title);
