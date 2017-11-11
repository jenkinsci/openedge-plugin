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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;

public class OpenEdgeAxis extends Axis {

  public OpenEdgeAxis(String name, List<String> values) {
    super(name, values);
  }

  @DataBoundConstructor
  public OpenEdgeAxis(String[] values) {
    super("oeaxis", Arrays.asList(values));
  }

  @Extension
  public static class DescriptorImpl extends AxisDescriptor {
    @CopyOnWrite
    private volatile OpenEdgeInstallation[] installations = new OpenEdgeInstallation[0];

    public DescriptorImpl() {
      load();
    }

    @Override
    public String getDisplayName() {
      return "OpenEdge";
    }

    public List<OpenEdgeInstallation> getOpenEdgeInstallations() {
      List<OpenEdgeInstallation> list = new ArrayList<>();
      for (ToolDescriptor<?> desc : ToolInstallation.all()) {
        for (ToolInstallation inst : desc.getInstallations()) {
          if (inst instanceof OpenEdgeInstallation) {
            list.add((OpenEdgeInstallation) inst);
          }
        }
      }
      return list;
    }

    @Override
    public boolean isInstantiable() {
      return !getOpenEdgeInstallations().isEmpty();
    }

    public void setInstallations(OpenEdgeInstallation... installations) {
      this.installations = installations;
      save();
    }

  }
}
