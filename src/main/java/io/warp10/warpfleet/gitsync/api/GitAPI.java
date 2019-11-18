package io.warp10.warpfleet.gitsync.api;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * The type Git api.
 */
public class GitAPI {
  private static Logger LOG = LoggerFactory.getLogger(GitAPI.class);

  private String macrosPath;
  private String tmpPath;

  /**
   * Instantiates a new Git api.
   *
   * @param macrosPath the macros path
   * @param tmpPath    the tmp path
   */
  public GitAPI(String macrosPath, String tmpPath) {
    LOG.debug("Initialize with: " + macrosPath + " | " + tmpPath);
    this.macrosPath = macrosPath;
    this.tmpPath = tmpPath;
    File repo = new File(this.macrosPath);
    if (!repo.exists()) {
      repo.mkdirs();
    }
    File tmp = new File(this.tmpPath);
    if (!tmp.exists()) {
      tmp.mkdirs();
    }
  }

  private static boolean testMC2(Path source) {
    LOG.trace(source.toFile().getName() + " -> " + source.toFile().getName().endsWith("mc2"));
    return source.toFile().getName().endsWith("mc2");
  }

  /**
   * Clone or pull boolean.
   *
   * @param remote the remote git repository
   * @return the status
   * @throws GitAPIException the git api exception
   * @throws IOException     the io exception
   */
  public boolean cloneOrPull(JSONObject remote) throws GitAPIException, IOException {
    List<String> path = Arrays.asList(remote.getString("url").split("/"));
    Collections.reverse(path);
    File dir = new File(this.tmpPath + File.separator + path.get(0).replaceAll("\\.git", ""));
    if (!dir.exists()) {
      return this.clone(remote, dir);
    } else {
      return this.pull(dir);
    }
  }

  private void copyFolder(Path src) throws IOException {
    LOG.debug("CopyFolder " + src.toAbsolutePath());
    Files.walk(src)
        .filter(GitAPI::testMC2)
        .forEach(source -> copy(source, Path.of(new File(this.macrosPath).getAbsolutePath()).resolve(src.relativize(source))));
  }

  private void copy(Path source, Path dest) {
    LOG.trace("Copy from " + source.toAbsolutePath() + " to " + dest.toAbsolutePath());
    try {
      dest.getParent().toFile().mkdirs();
      Files.copy(source, dest, REPLACE_EXISTING);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private boolean pull(File dest) throws IOException {
    LOG.debug("Pulling " + dest.getName());
    new Git(new FileRepositoryBuilder().setGitDir(dest).readEnvironment().findGitDir().build()).pull();
    this.copyFolder(Path.of(dest.getAbsolutePath()));
    LOG.debug("Pull done");
    return true;
  }

  private boolean clone(JSONObject remote, File dest) throws GitAPIException, IOException {
    LOG.debug("Cloning " + dest.getName());
    Git.cloneRepository()
        .setURI(remote.getString("url"))
        .setDirectory(dest)
        .setBranch(remote.optString("branch",  "master"))
        .setTransportConfigCallback(transport -> {
          SshTransport sshTransport = (SshTransport) transport;
          sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
              session.setUserInfo(new UserInfo() {
                @Override
                public String getPassphrase() {
                  return remote.optString("passphrase", null);
                }

                @Override
                public String getPassword() {
                  return remote.optString("password", null);
                }

                @Override
                public boolean promptPassword(String message) {
                  return false;
                }

                @Override
                public boolean promptPassphrase(String message) {
                  return true;
                }

                @Override
                public boolean promptYesNo(String message) {
                  return false;
                }

                @Override
                public void showMessage(String message) {
                }
              });
            }
          });
        })
        .call();
    this.copyFolder(Path.of(dest.getAbsolutePath()));
    LOG.debug("Clone done");
    return true;
  }
}
