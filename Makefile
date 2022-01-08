preview-docs:
	cd dilono-basic-sample && ../mvnw clean generate-resources site:deploy

publish-docs:
	./mvnw clean install
	cd dilono-basic-sample && ../mvnw clean generate-resources site:deploy -Ddocs.version=""
