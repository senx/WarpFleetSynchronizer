package io.warp10.warpfleet.synchronizer.api;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The type Warp fleet service.
 */
public class RepositoriesManager {
  private static Logger LOG = LoggerFactory.getLogger(RepositoriesManager.class);
  private JSONObject conf;
  private String macroPath;
  private String confPath;
  private GitAPI gitAPI;

  /**
   * Instantiates a new Warp fleet service.
   *
   * @param confPath  the conf path
   * @param macroPath the macro path
   * @throws IOException the io exception
   */
  public RepositoriesManager(String confPath, String macroPath) throws IOException {
    LOG.info("Initialization: " + confPath);
    this.confPath = confPath;
    this.conf = new JSONObject(FileUtils.readFileToString(new File(confPath), "UTF-8"));
    this.macroPath = macroPath;
    this.gitAPI = new GitAPI(this.macroPath, "tmp");
  }

  /**
   * Add repository boolean.
   *
   * @param repo the repo
   * @return the boolean
   * @throws IOException the io exception
   */
  public boolean addRepository(JSONObject repo) throws IOException {
    AtomicBoolean status = new AtomicBoolean(true);
    this.getRepos().forEach(r -> status.set(!repo.getString("name").equals(repo.getString("name"))));
    if (status.get()) {
      this.conf.getJSONArray("repos").put(repo);
      FileUtils.writeStringToFile(new File(this.confPath), this.conf.toString(), "UTF-8");
    }
    return status.get();
  }

  /**
   * Delete repository boolean.
   *
   * @param repoName the repo name
   * @return the boolean
   * @throws IOException the io exception
   */
  public boolean deleteRepository(String repoName) throws IOException {
    JSONArray newRepoList = new JSONArray();
    this.getRepos().forEach(r -> {
      if (!((JSONObject) r).getString("name").equals(repoName)) {
        newRepoList.put(r);
      }
    });
    this.conf.put("repos", newRepoList);
    FileUtils.writeStringToFile(new File(this.confPath), this.conf.toString(), "UTF-8");
    return true;
  }


  /**
   * Sync all boolean.
   *
   * @return the boolean
   * @throws GitAPIException the git api exception
   * @throws IOException     the io exception
   */
  public boolean syncAll() throws GitAPIException, IOException {
    LOG.debug("Synchronize all repositories");
    JSONArray repos = this.conf.optJSONArray("repos");
    boolean status = true;
    if (null == repos) {
      LOG.warn("No repositories configured");
      repos = new JSONArray();
    }
    for (Object repo: repos) {
      LOG.debug("Synchronizing: " + ((JSONObject) repo).optString("name", "unknown"));
      status = gitAPI.cloneOrPull(((JSONObject) repo)) && status;
      LOG.debug("Status: " + status);
    }
    return status;
  }

  /**
   * Sync boolean.
   *
   * @param repository the repository
   * @return the boolean
   * @throws GitAPIException the git api exception
   * @throws IOException     the io exception
   */
  public boolean sync(String repository) throws GitAPIException, IOException {
    LOG.debug("Synchronizing: " + repository);
    JSONArray repos = conf.optJSONArray("repos");
    boolean status = true;
    if (null == repos) {
      LOG.warn("No repositories configured");
      repos = new JSONArray();
    }
    JSONObject remote = null;
    for (Object repo: repos) {
      if (((JSONObject) repo).getString("name").equals(repository)) {
        LOG.debug("Found: " + repo.toString());
        remote = ((JSONObject) repo);
      }
    }
    if (null != remote) {
      status = gitAPI.cloneOrPull(remote);
      LOG.debug("Status: " + status);
    } else {
      LOG.warn("No remote found");
    }
    return status;
  }

  /**
   * Gets repos.
   *
   * @return the repos
   */
  public JSONArray getRepos() {
    LOG.debug("Get all repositories description");
    JSONArray response = new JSONArray();
    JSONArray repos = conf.optJSONArray("repos");
    if (repos != null) {
      for (Object rep: conf.getJSONArray("repos")) {
        JSONObject r = (JSONObject) rep;
        response.put(
            new JSONObject()
                .put("name", r.getString("name"))
                .put("url", r.getString("url"))
                .put("branch", r.optString("branch", "master"))
        );
      }
    } else {
      LOG.warn("No repositories configured");
    }
    return response;
  }

  /**
   * Gets repo.
   *
   * @param repoName the repo name
   * @return the repo
   */
  public JSONObject getRepo(String repoName) {
    AtomicReference<JSONObject> repo = new AtomicReference<>(new JSONObject());
    this.getRepos().forEach(r -> {
      if (((JSONObject) r).getString("name").equals(repoName)) {
        repo.set((JSONObject) r);
      }
    });
    return repo.get();
  }

  /**
   * Update repository boolean.
   *
   * @param repoName the repo name
   * @param repo     the repo
   * @return the boolean
   * @throws IOException the io exception
   */
  public boolean updateRepository(String repoName, JSONObject repo) throws IOException {
    return this.deleteRepository(repoName) && this.addRepository(repo.put("name", repoName));
  }

  /**
   * Gets conf.
   *
   * @return the conf
   */
  public JSONObject getConf() {
    return conf;
  }

  /**
   * Gets macro path.
   *
   * @return the macro path
   */
  public String getMacroPath() {
    return macroPath;
  }

  public JSONObject reloadConf() {
    JSONObject status = new JSONObject();
    try {
      this.conf = new JSONObject(FileUtils.readFileToString(new File(this.confPath), "UTF-8"));
      return status.put("status", true);
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      return status.put("status", false).put("message", e.getMessage());
    }
  }
}
