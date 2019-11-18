# WarpFleetSynchronizer v0.0.1

This Web server aims to synchronize git repositories and serve macros for the WarpFleet resolver

- [WarpFleetSynchronizer](#warpfleetsynchronizer)
	- [synchronize all](#synchronize-all)
	- [synchronize a particule repo](#synchronize-a-particule-repo)
	- [Request all configured repos](#request-all-configured-repos)
	


# WarpFleetSynchronizer

## synchronize all



	GET /api/sync


## synchronize a particule repo



	GET /api/sync/:repo


### Parameters

| Name    | Type      | Description                          |
|---------|-----------|--------------------------------------|
| repo			| String			|  <p>Repository's name.</p>							|

## Request all configured repos



	GET /api/repos



