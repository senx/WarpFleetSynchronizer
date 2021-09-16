# WarpFleet™ Synchronizer

This Web server aims to synchronize git repositories and serve macros for the WarpFleet™ resolver

## Configuration sample

````json
{
  "host": "0.0.0.0",
  "port": 8082,
  "remotes": "www.myWebSite.com", // independant of your Warp 10 instance, for admin purpose
  "repos" : [
    {
      "owner": "hammet",
      "name": "warpfleet-macros", // must be unique
      "url": "git@gitlab.com:senx/warpfleet-macros.git",
      "passphrase": "sshPass"
    },
    {
      "owner": "hetfield",
      "name": "warpfleet-macros2",
      "url": "https://gitlab.com/senx/warpfleet-macros.git",
      "username": "oauth2",
      "password": "<gitlab oAuth Token>",
      "branch": "main"
    }
  ]
}
````

## Run 

`java -jar WarpFleetSynchronizer.jar ./path/to/conf.json`

Now listen at 0.0.0.0:8082

## Usage

### For WarpFleet Resolver

    http://localhost:8082/macros/path/to/macro.mc2


### API

Sync all

    http://localhost:8082/api/sync
    
Sync specific repo

    http://localhost:8082/api/sync/myRepo

List repositories

    http://localhost:8082/api/repos/<owner>


> Copyright 2020  SenX S.A.S.
>
> [https://senx.io](https://senx.io)

