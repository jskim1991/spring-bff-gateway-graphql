## 1. Using cURL
```shell
$ curl \
    -X POST \
    -H "Content-Type: application/json" \
    -d '{"query": "query { movieInfos { movieInfoId name year cast release_date reviews { reviewId movieInfoId comment rating } } }" }' \
http://localhost:9000/graphql
```


## 2. Using GraphiQL
http://localhost:9000/graphiql
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