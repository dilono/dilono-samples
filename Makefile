preview-docs:
	cd dilono-basic-sample && ../mvnw clean generate-resources site:deploy

publish-docs:
	cd dilono-basic-sample && ../mvnw clean generate-resources site:deploy -Ddocs.version=""
