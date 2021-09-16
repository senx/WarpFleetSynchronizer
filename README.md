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

## Run as service

Add `/etc/systemd/system/warpfleet-synchronizer.service` with following content (adjust paths to your needs):

```
[Unit]
Description=Warp 10 - WarpFleet Synchronizer
Documentation=https://github.com/senx/WarpFleetSynchronizer
After=network-online.target

[Service]
Type=simple
User=warp10
Group=warp10
WorkingDirectory=/path/to/warpfleet/synchronizer
ExecStart=java -jar /path/to/warpfleet/synchronizer/bin/WarpFleetSynchronizer-all.jar /path/to/warpfleet/synchronizer/conf/synchronizer.conf
Restart=on-failure
SuccessExitStatus=143 

[Install]
WantedBy=multi-user.target
```
Then start service and enable at boot time:

```commandline
sudo systemctl start warpfleet-synchronizer
sudo systemctl enable warpfleet-synchronizer
```
Check it works as expected:

```commandline
sudo systemctl status warpfleet-synchronizer
or
journalctl -fu warpfleet-synchronizer
```

Note:
* Clones of git repositories will be stored in `/path/to/warpfleet/synchronizer/tmp`
* Macros will be stored in `/path/to/warpfleet/synchronizer/macros/macros/<repo>` 


## Usage

### For WarpFleet Resolver

    http://localhost:8082/macros/<repo name>/path/to/macro.mc2


### API

Sync all

    http://localhost:8082/api/sync
    
Sync specific repo

    http://localhost:8082/api/sync/myRepo

List repositories

    http://localhost:8082/api/repos/<owner>


> Copyright 2019-2021  SenX S.A.S.
>
> [https://senx.io](https://senx.io)

