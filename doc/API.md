<a name="top"></a>
# WarpFleetSynchronizer v0.0.1

This Web server aims to synchronize git repositories and serve macros for the WarpFleet resolver

# WarpFleetSynchronizer

<h1>WarpFleet™ Synchronizer</h1>
<p>This Web server aims to synchronize git repositories and serve macros for the WarpFleet™ resolver</p>
<h2>Configuration sample</h2>
<pre><code class="language-json">{
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
</code></pre>
<h2>Run</h2>
<p><code>java -jar WarpFleetSynchronizer.jar ./path/to/conf.json</code></p>
<p>Now listen at 0.0.0.0:8082</p>
<h2>Run as service</h2>
<p>Add <code>/etc/systemd/system/warpfleet-synchronizer.service</code> with following content (adjust paths to your needs):</p>
<pre><code>[Unit]
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
</code></pre>
<p>Then start service and enable at boot time:</p>
<pre><code class="language-commandline">sudo systemctl start warpfleet-synchronizer
sudo systemctl enable warpfleet-synchronizer
</code></pre>
<p>Check it works as expected:</p>
<pre><code class="language-commandline">sudo systemctl status warpfleet-synchronizer
or
journalctl -fu warpfleet-synchronizer
</code></pre>
<p>Note:</p>
<ul>
<li>Clones of git repositories will be stored in <code>/path/to/warpfleet/synchronizer/tmp</code></li>
<li>Macros will be stored in <code>/path/to/warpfleet/synchronizer/macros/macros/&lt;repo&gt;</code></li>
</ul>
<h2>Usage</h2>
<h3>Test in your browser</h3>
<pre><code>http://localhost:8082/macros/&lt;repo name&gt;/path/to/macro.mc2
</code></pre>
<h3>For WarpFleet Resolver</h3>
<pre><code>http://localhost:8082/macros/
</code></pre>
<h3>API</h3>
<p>Sync all</p>
<pre><code>http://localhost:8082/api/sync
</code></pre>
<p>Sync specific repo</p>
<pre><code>http://localhost:8082/api/sync/myRepo
</code></pre>
<p>List repositories</p>
<pre><code>http://localhost:8082/api/repos/&lt;owner&gt;
</code></pre>
<blockquote>
<p>Copyright 2019-2021  SenX S.A.S.</p>
<p><a href="https://senx.io">https://senx.io</a></p>
</blockquote>


# Table of contents

- [WarpFleetSynchronizer](#WarpFleetSynchronizer)
  - [Add a new repository](#Add-a-new-repository)
  - [delete a repository by its name](#delete-a-repository-by-its-name)
  - [fetch a repository by its name](#fetch-a-repository-by-its-name)
  - [Reload configuration file](#Reload-configuration-file)
  - [Request all configured repos](#Request-all-configured-repos)
  - [synchronize a particular repo](#synchronize-a-particular-repo)
  - [synchronize all](#synchronize-all)
  - [update a repository by its name](#update-a-repository-by-its-name)

___


# <a name='WarpFleetSynchronizer'></a> WarpFleetSynchronizer

## <a name='Add-a-new-repository'></a> Add a new repository
[Back to top](#top)

```
PUT /api/repos
```

### Parameters - `Parameter`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| repository | `Object` | <p>Repository.</p> |
| owner | `String` | <p>Repository owner id.</p> |
### Success response

#### Success response - `Success 200`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| status | `Object` | <p>status.</p> |

## <a name='delete-a-repository-by-its-name'></a> delete a repository by its name
[Back to top](#top)

```
DELETE /api/repos/:repo
```

### Parameters - `Parameter`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| repo | `String` | <p>Repository name.</p> |
| owner | `String` | <p>Repository owner id.</p> |
### Success response

#### Success response - `Success 200`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| status | `Object` | <p>status.</p> |

## <a name='fetch-a-repository-by-its-name'></a> fetch a repository by its name
[Back to top](#top)

```
GET /api/repos/:repo
```

### Parameters - `Parameter`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| repo | `String` | <p>Repository name.</p> |
| owner | `String` | <p>Repository owner id.</p> |
### Success response

#### Success response - `Success 200`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| repository | `Object` | <p>Repository.</p> |

## <a name='Reload-configuration-file'></a> Reload configuration file
[Back to top](#top)

```
GET /api/reload
```
### Success response

#### Success response - `Success 200`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| status | `Object` | <p>status.</p> |

## <a name='Request-all-configured-repos'></a> Request all configured repos
[Back to top](#top)

```
GET /api/repos
```

### Parameters - `Parameter`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| owner | `String` | <p>Repository owner id.</p> |
### Success response

#### Success response - `Success 200`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| repos | `Object[]` | <p>list of git repositories.</p> |

## <a name='synchronize-a-particular-repo'></a> synchronize a particular repo
[Back to top](#top)

```
GET /api/sync/:repo
```

### Parameters - `Parameter`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| repo | `String` | <p>Repository name.</p> |
### Success response

#### Success response - `Success 200`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| status | `Object` | <p>status.</p> |

## <a name='synchronize-all'></a> synchronize all
[Back to top](#top)

```
GET /api/sync
```
### Success response

#### Success response - `Success 200`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| status | `Object` | <p>status.</p> |

## <a name='update-a-repository-by-its-name'></a> update a repository by its name
[Back to top](#top)

```
PUT /api/repos
```

### Parameters - `Parameter`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| repository | `Object` | <p>Repository.</p> |
| owner | `String` | <p>Repository owner id.</p> |
| repo | `String` | <p>Repository name.</p> |
### Success response

#### Success response - `Success 200`

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| status | `Object` | <p>status.</p> |

