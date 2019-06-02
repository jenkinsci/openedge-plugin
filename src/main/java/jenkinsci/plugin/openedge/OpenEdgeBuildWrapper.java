/**
 * MIT License
 *
 * Copyright (c) 2017 Riverside Software
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jenkinsci.plugin.openedge;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.AbortException;
import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

/**
 * @author Gilles QUERRET
 */
public class OpenEdgeBuildWrapper extends BuildWrapper {
  private String openEdgeInstall;

  @DataBoundConstructor
  public OpenEdgeBuildWrapper(String openEdgeInstall) {
    this.openEdgeInstall = openEdgeInstall;
  }

  @Override
  public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener)
      throws IOException, InterruptedException {
    OpenEdgeInstallation installation = getOpenEdgeInstallation();
    if (installation == null) {
      throw new AbortException("No such OpenEdge installation: '" + openEdgeInstall + "'");
    }
    Computer currComp = Computer.currentComputer();
    if (currComp != null) {
      Node node = currComp.getNode();
      if (node != null)
        installation = installation.forNode(node, listener);
    }

    // Add DLC variable and modify PATH
    final OpenEdgeInstallation install = installation;
    return new Environment() {
      @Override
      public void buildEnvVars(Map<String, String> env) {
        if (install != null) {
          EnvVars envVars = new EnvVars();
          install.buildEnvVars(envVars);
          env.putAll(envVars);
        }
      }
    };
  }

  private OpenEdgeInstallation getOpenEdgeInstallation() {
    for (OpenEdgeInstallation i : ((DescriptorImpl) getDescriptor()).getInstallations()) {
      if (i.getName().equals(openEdgeInstall)) {
        return i;
      }
    }

    return null;
  }

  @Extension
  public static final class DescriptorImpl extends BuildWrapperDescriptor {
    @CopyOnWrite
    private volatile OpenEdgeInstallation[] installations = new OpenEdgeInstallation[0];

    public DescriptorImpl() {
      load();
    }

    @Override
    public String getDisplayName() {
      return "OpenEdge";
    }

    @Override
    public boolean isApplicable(AbstractProject<?, ?> item) {
      return true;
    }

    public OpenEdgeInstallation[] getInstallations() {
      return Arrays.copyOf(installations, installations.length);
    }

    public void setInstallations(OpenEdgeInstallation... installations) {
      this.installations = installations;
      save();
    }
  }

}
