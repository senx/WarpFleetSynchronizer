/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.warp10.warpfleet.gitsync;

import io.warp10.warpfleet.gitsync.api.GitAPI;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.io.File;
import java.io.IOException;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.ipAddress;
import static spark.Spark.notFound;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static spark.Spark.threadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Warp fleet synchronizer.
 */
public class WarpFleetSynchronizer {
  private static String MACROS_PATH = "macros";
  private static String TMP_PATH = "tmp";
  private static JSONObject CONF;
  private static GitAPI gitAPI = new GitAPI(MACROS_PATH, TMP_PATH);
  private static Logger LOG = LoggerFactory.getLogger(WarpFleetSynchronizer.class);

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  @SuppressWarnings("DanglingJavadoc")
  public static void main(String[] args) {
    if (args.length != 1) {
      LOG.error("Missing configuration file");
      System.exit(1);
    }
    try {
      CONF = init(args[0]);

      port(CONF.optInt("port", 8080));
      ipAddress(CONF.optString("host", "0.0.0.0"));
      threadPool(8);
      // Serving macros
      staticFiles.externalLocation(new File(MACROS_PATH).getAbsolutePath());

      // Before filter
      before("/*", (req, res) -> res.header("Content-Type", "text/plain"));
      before("/api/*", (req, res) -> res.header("Content-Type", "application/json"));

      /**
       * @api {get} /api/repos Request all configured repos
       * @apiName getRepos
       * @apiGroup WarpFleetSynchronizer
       *
       * @apiSuccess {Object[]}  repos list of git repositories.
       */
      get("/api/repos", WarpFleetSynchronizer::getRepos);

      /**
       * @api {get} /api/sync/:repo synchronize a particule repo
       * @apiName SyncRepo
       * @apiGroup WarpFleetSynchronizer
       *
       * @apiParam {String} repo Repository's name.
       *
       * @apiSuccess {Object}  status status.
       */
      get("/api/sync/:repo", WarpFleetSynchronizer::sync);

      /**
       * @api {get} /api/sync synchronize all
       * @apiName SyncAll
       * @apiGroup WarpFleetSynchronizer
       *
       * @apiSuccess {Object}  status status.
       */
      get("/api/sync", WarpFleetSynchronizer::syncAll);

      // exception catching
      exception(Exception.class, (e, req, res) -> {
        LOG.error(e.getMessage(), e);
        res.status(500);
        res.body(new JSONObject().put("message", e.getMessage()).toString());
      });

      exception(GitAPIException.class, (e, req, res) -> {
        LOG.error(e.getMessage(), e);
        res.status(501);
        res.body(new JSONObject().put("message", e.getMessage()).toString());
      });

      // 404 catching
      notFound((req, res) -> {
        res.status(404);
        return new JSONObject().put("message", "Not found");
      });

    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      System.exit(1);
    }
  }

  private static JSONObject syncAll(Request request, Response response) throws GitAPIException, IOException {
    LOG.debug("Synchronize all repositories");
    JSONArray repos = CONF.optJSONArray("repos");
    boolean status = true;
    if (null == repos) {
      LOG.warn("No repositories configured");
      repos = new JSONArray();
    }
    for (Object repo: repos) {
      JSONObject r = (JSONObject) repo;
      LOG.debug("Synchronizing: " + r.optString("name", "unknown"));
      status = gitAPI.cloneOrPull(r) && status;
      LOG.debug("Status: " + status);
    }
    return new JSONObject().put("status", status);
  }

  private static JSONObject sync(Request req, Response res) throws GitAPIException, IOException {
    LOG.debug("Synchronizing: " + req.params(":repo"));
    JSONArray repos = CONF.optJSONArray("repos");
    boolean status = true;
    if (null == repos) {
      LOG.warn("No repositories configured");
      repos = new JSONArray();
    }
    JSONObject remote = null;
    for (Object repo: repos) {
      JSONObject r = (JSONObject) repo;
      if (r.getString("name").equals(req.params(":repo"))) {
        LOG.debug("Found: " + r.toString());
        remote = r;
      }
    }
    if (null != remote) {
      status = gitAPI.cloneOrPull(remote);
      LOG.debug("Status: " + status);
    } else {
      LOG.warn("No remote found");
    }
    return new JSONObject().put("status", status);
  }

  private static JSONArray getRepos(Request req, Response res) {
    LOG.debug("Get all repositories description");
    JSONArray response = new JSONArray();
    JSONArray repos = CONF.optJSONArray("repos");
    if (repos != null) {
      for (Object rep: CONF.getJSONArray("repos")) {
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

  private static JSONObject init(String confPath) throws IOException {
    LOG.info("Initialization: " + confPath);
    String jsonStr = FileUtils.readFileToString(new File(confPath), "UTF-8");
    return new JSONObject(jsonStr);
  }
}
