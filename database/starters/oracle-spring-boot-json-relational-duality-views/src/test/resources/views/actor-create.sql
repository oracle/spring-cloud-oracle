create force editionable json relational duality view actor_dv as actor @insert {
  _id : actor_id
  firstName : first_name
  lastName : last_name
  movies : movie_actor @insert [ {
    movie @unnest @insert {
      _id : movie_id
      title
      releaseYear : release_year
      genre
    }
  } ]
}