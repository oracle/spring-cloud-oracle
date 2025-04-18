create force editionable json relational duality view actor_dv as actor {
  _id : actor_id
  firstName : first_name
  lastName : last_name
  movies : movie_actor [ {
    movie @unnest {
      _id : movie_id
      title
      releaseYear : release_year
      genre
      director {
        _id : director_id
        firstName : first_name
        lastName : last_name
        director_bio {
          _id : director_id
          biography
        }
      }
    }
  } ]
}