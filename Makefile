tests:
	cd movies-info-service && mvn clean test
	cd movies-review-service && mvn clean test
	cd gateway && mvn clean test