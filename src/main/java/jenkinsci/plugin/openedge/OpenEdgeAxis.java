package jenkinsci.plugin.openedge;

import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import hudson.matrix.MatrixBuild.MatrixBuildExecution;
import hudson.model.Computer;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jenkinsci.plugin.openedge.OpenEdgeBuildWrapper.DescriptorImpl;

import org.kohsuke.stapler.DataBoundConstructor;

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
    		List<OpenEdgeInstallation> list = new ArrayList<OpenEdgeInstallation>();
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
    	
		public OpenEdgeInstallation[] getInstallations() {
			return installations;
		}

		public void setInstallations(OpenEdgeInstallation... installations) {
			this.installations = installations;
			save();
		}

    }
}
