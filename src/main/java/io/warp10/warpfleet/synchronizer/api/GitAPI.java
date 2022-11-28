/*
 *  Copyright 2022  SenX S.A.S.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http:www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.warp10.warpfleet.synchronizer.api;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.walk;

/**
 * The type Git api.
 */
public class GitAPI {
  private static final Logger LOG = LoggerFactory.getLogger(GitAPI.class);

  private final String macrosPath;
  private final String tmpPath;

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
      boolean mkDirs = repo.mkdirs();
      LOG.debug("mkDirs " + repo.getAbsolutePath() + ": " + mkDirs);
    }
    File tmp = new File(this.tmpPath);
    if (!tmp.exists()) {
      boolean mkDirs = tmp.mkdirs();
      LOG.debug("mkDirs " + tmp.getAbsolutePath() + ": " + mkDirs);
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
    File dir = new File(this.tmpPath + File.separator + remote.getString("name"));
    if (!dir.exists()) {
      return this.clone(remote, dir);
    } else {
      return this.pull(remote, dir);
    }
  }

  private void copyFolder(Path src, String prefix) throws IOException {
    Path dest = Paths.get(new File(this.macrosPath).getAbsolutePath() + File.separator + prefix);
    LOG.debug("Clean " + dest);
    FileUtils.deleteDirectory(dest.toFile());
    FileUtils.forceMkdir(dest.toFile());

    LOG.debug("CopyFolder " + src.toAbsolutePath() + " to " + prefix);
    walk(src.toAbsolutePath())
        .filter(GitAPI::testMC2)
        .forEach(source -> copy(source, dest.resolve(src.relativize(source))));
  }

  private void copy(Path source, Path dest) {
    LOG.trace("Copy from " + source.toAbsolutePath() + " to " + dest.toAbsolutePath());
    try {
      boolean mkDirs = dest.toFile().getParentFile().mkdirs();
      LOG.debug("mkDirs " + dest.toFile().getParentFile().getAbsolutePath() + ": " + mkDirs);
      FileUtils.copyFile(source.toFile(), dest.toFile(), false);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private boolean pull(JSONObject remote, File dest) throws IOException, GitAPIException {
    LOG.debug("Pulling " + dest.getName());
    PullCommand git = new Git(new RepositoryBuilder().findGitDir(dest).build()).pull();
    if (remote.getString("url").startsWith("http")) {
      if (!"".equals(remote.optString("username", ""))) {
        git.setCredentialsProvider(
            new UsernamePasswordCredentialsProvider(
                remote.optString("username", ""),
                remote.optString("password", "")
            )
        );
      }
    } else {
      git.setTransportConfigCallback(setCredentials(remote));
    }
    PullResult result = git.call();
    this.copyFolder(Paths.get(dest.getAbsolutePath()), remote.getString("name"));
    LOG.debug("Pull done: " + result.isSuccessful());
    return result.isSuccessful();
  }

  private boolean clone(JSONObject remote, File dest) throws GitAPIException, IOException {
    LOG.debug("Cloning " + dest.getName());
    CloneCommand git = Git.cloneRepository()
        .setURI(remote.getString("url"))
        .setDirectory(dest)
        .setBranch(remote.optString("branch", "master"));
    if (remote.getString("url").startsWith("http")) {
      if (!"".equals(remote.optString("username", ""))) {
        git.setCredentialsProvider(
            new UsernamePasswordCredentialsProvider(
                remote.optString("username", ""),
                remote.optString("password", "")
            )
        );
      }
    } else {
      git.setTransportConfigCallback(this.setCredentials(remote));
    }
    git.call();
    this.copyFolder(Paths.get(dest.getAbsolutePath()), remote.getString("name"));
    LOG.debug("Clone done");
    return true;
  }

  private TransportConfigCallback setCredentials(JSONObject remote) {
    return transport -> {
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
              LOG.info(message);
            }
          });
        }
      });
    };
  }
}
