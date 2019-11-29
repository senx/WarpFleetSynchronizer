# WarpFleetSynchronizer v0.0.1

This Web server aims to synchronize git repositories and serve macros for the WarpFleet resolver

- [WarpFleetSynchronizer](#warpfleetsynchronizer)
	- [Add a new repository](#add-a-new-repository)
	- [delete a repository by its name](#delete-a-repository-by-its-name)
	- [fetch a repository by its name](#fetch-a-repository-by-its-name)
	- [Request all configured repos](#request-all-configured-repos)
	- [synchronize a particular repo](#synchronize-a-particular-repo)
	- [synchronize all](#synchronize-all)
	- [update a repository by its name](#update-a-repository-by-its-name)
	


# WarpFleetSynchronizer

## Add a new repository



	PUT /api/repos


### Parameters

| Name    | Type      | Description                          |
|---------|-----------|--------------------------------------|
| repository			| Object			|  <p>Repository.</p>							|

## delete a repository by its name



	DELETE /api/repos/:repo


### Parameters

| Name    | Type      | Description                          |
|---------|-----------|--------------------------------------|
| repo			| String			|  <p>Repository name.</p>							|

## fetch a repository by its name



	GET /api/repos/:repo


### Parameters

| Name    | Type      | Description                          |
|---------|-----------|--------------------------------------|
| repo			| String			|  <p>Repository name.</p>							|

## Request all configured repos



	GET /api/repos


## synchronize a particular repo



	GET /api/sync/:repo


### Parameters

| Name    | Type      | Description                          |
|---------|-----------|--------------------------------------|
| repo			| String			|  <p>Repository name.</p>							|

## synchronize all



	GET /api/sync


## update a repository by its name



	PUT /api/repos


### Parameters

| Name    | Type      | Description                          |
|---------|-----------|--------------------------------------|
| repository			| Object			|  <p>Repository.</p>							|
| repo			| String			|  <p>Repository name.</p>							|


