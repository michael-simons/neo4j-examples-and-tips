MATCH (n) detach delete n;

CREATE (q:Artist:Band {name: 'Queen'})
CREATE (anato:Album {name: 'A Night At The Opera'}) - [:RELEASED_BY] -> (q)
CREATE (innuendo:Album {name: 'Innuendo'}) - [:RELEASED_BY] -> (q)
MERGE (gb:Country {code: 'GB', name: 'Great Britain'})
MERGE (tz:Country {code: 'TZ', name: 'Tanzania'})
WITH q, gb, tz
MERGE (q) - [:FOUNDED_IN] -> (gb)

MERGE (fm:Artist:SoloArtist {name: 'Freddie Mercury'})
MERGE (fm) - [:BORN_IN] -> (tz)
MERGE (q) - [:HAS_MEMBER {joinedIn: 1970, leftIn: 1991}] -> (fm)

MERGE (rmt:Artist:SoloArtist {name: 'Roger Taylor'})
MERGE (rmt) - [:BORN_IN] -> (gb)
MERGE (q) - [:HAS_MEMBER {joinedIn: 1970}] -> (rmt)

MERGE (bm:Artist:SoloArtist {name: 'Brian May'})
MERGE (bm) - [:BORN_IN] -> (gb)
MERGE (q) - [:HAS_MEMBER {joinedIn: 1970}] -> (bm)


MERGE (deacy:Artist:SoloArtist {name: 'John Deacon'})
MERGE (deacy) - [:BORN_IN] -> (gb)
MERGE (q) - [:HAS_MEMBER {joinedIn: 1971}] -> (deacy)


RETURN *;

MATCH (a:Album) -[:RELEASED_BY]-> (b:Band),
      (c) <-[:FOUNDED_IN]- (b) -[:HAS_MEMBER]-> (m) -[:BORN_IN]-> (c2)
WHERE a.name = 'Innuendo'
RETURN b, m, c, c2