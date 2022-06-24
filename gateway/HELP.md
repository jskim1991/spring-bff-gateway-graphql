```graphql
query {
  movieInfos {
    movieInfoId,
    name,
    year,
    cast,
    release_date,
    reviews {
      reviewId,
      movieInfoId,
      comment,
      rating
    }
  }
}
```