# WarpFleet Synchronizer

This Web server aims to synchronize git repositories and serve macros for the WarpFleet resolver

## Configuration sample

````json
{
  "host": "0.0.0.0",
  "port": 8080,
  "repos" : [
    {
      "name": "warpfleet-macros",
      "url": "git@gitlab.com:senx/warpfleet-macros.git",
      "passphrase": "sshPass"
    }
  ]
}
````

## Run 

`java -jar WarpFleetSynchronizer.jar ./path/to/conf.json`

Now listen at 0.0.0.0:8080

## Usage

### For WarpFleet Resolver

    http://localhost:8080/path/to/macro.mc2


### API

Sync all

    http://localhost:8080/api/sync
    
Sync specific repo

    http://localhost:8080/api/sync/myRepo

List repositories

    http://localhost:8080/api/repos
> Copyright 2019  SenX S.A.S.
>
> [https://senx.io](https://senx.io)

