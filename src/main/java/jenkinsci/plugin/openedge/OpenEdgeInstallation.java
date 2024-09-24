/**
 * MIT License
 *
 * Copyright (c) 2017 Riverside Software
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package jenkinsci.plugin.openedge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.io.Files;

import eu.rssw.pct.RCodeInfo;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;

/**
 * @author Gilles Querret
 */
public final class OpenEdgeInstallation extends ToolInstallation
    implements NodeSpecific<OpenEdgeInstallation> {
  private static final String MAIN_PATTERN = "(?:[a-zA-Z]+\\s+)+((\\d+)(?:[A-Z0-9\\u002E])*)\\s+as of(.*)";
  private static final long serialVersionUID = -1639511702015070000L;

  @DataBoundConstructor
  public OpenEdgeInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
    super(name, home, properties);
  }

  @Override
  public OpenEdgeInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
    return new OpenEdgeInstallation(getName(), translateFor(node, log), getProperties().toList());
  }

  @Override
  public void buildEnvVars(EnvVars env) {
    String root = getHome();
    if (root != null) {
      env.put("DLC", root);
      env.put("PATH+DLC", new File(root, "bin").toString());
    }
  }

  /**
   * Don't use on agent OpenEdgeInstallation
   * 
   * @return The major version number or an empty string if not found
   */
  public String getVersionNumber() {
    try {
      String root = getHome();
      if (root != null)
        return parseVersionNumber(extractVersionString(new File(root)));
      else
        return "";
    } catch (IOException caught) {
      return "";
    }
  }

  public String getArch() {
    String root = getHome();
    if (root == null)
      return "";

    try (InputStream input = new FileInputStream(new File(root, "tty/prostart.r"))) {
      RCodeInfo rcode = new RCodeInfo(input);
      return (rcode.is64bits() ? "64" : "32");
    } catch (IOException | RCodeInfo.InvalidRCodeException caught) {
      return "";
    }
  }

  private static String extractVersionString(File dir) throws IOException {
    return Files.asCharSource(new File(dir, "version"), StandardCharsets.US_ASCII).readFirstLine();
  }

  private static String parseVersionNumber(String str) {
    Matcher m = Pattern.compile(MAIN_PATTERN).matcher(str);
    if (m.matches()) {
      return m.group(2);
    }

    return "";
  }

  @Extension
  @Symbol("openedge")
  public static class DescriptorImpl extends ToolDescriptor<OpenEdgeInstallation> {

    @Override
    public String getDisplayName() {
      return "OpenEdge";
    }

    @Override
    protected FormValidation checkHomeDirectory(File home) {
      if (new File(home, "progress.cfg").exists())
        return FormValidation.ok();
      else
        return FormValidation.error("No progress.cfg found");
    }

    @Override
    public OpenEdgeInstallation[] getInstallations() {
      return Jenkins.get().getDescriptorByType(OpenEdgeBuildWrapper.DescriptorImpl.class).getInstallations();
    }

    @Override
    public void setInstallations(OpenEdgeInstallation... installations) {
      Jenkins.get().getDescriptorByType(OpenEdgeBuildWrapper.DescriptorImpl.class).setInstallations(installations);
    }

  }

}
