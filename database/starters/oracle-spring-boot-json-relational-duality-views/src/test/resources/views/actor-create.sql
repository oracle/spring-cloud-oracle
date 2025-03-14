create force editionable json relational duality view actor_dv as actor {
  _id : actor_id
  firstName : first_name
  lastName : last_name
  movie_actor {
    movie_id
    actor_id
    movie : movie {
      _id : movie_id
      title
      releaseYear : release_year
      genre
      director : director {
        _id : director_id
        firstName : first_name
        lastName : last_name
        director_bio : director_bio {
          _id : director_id
          biography
        }
      }
    }
  }
}