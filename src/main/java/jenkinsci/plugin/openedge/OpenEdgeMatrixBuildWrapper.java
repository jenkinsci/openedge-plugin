/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Kyle Sweeney, Gregory Boissinot and other contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkinsci.plugin.openedge;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixProject;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gilles QUERRET
 */
public class OpenEdgeMatrixBuildWrapper extends BuildWrapper {
	private static Logger LOGGER = Logger.getLogger(OpenEdgeMatrixBuildWrapper.class.getName());

	@DataBoundConstructor
	public OpenEdgeMatrixBuildWrapper() {

	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		String oeAxis = build.getEnvironment(listener).get("oeaxis");
		LOGGER.info("OpenEdge axis : " + oeAxis);

		OpenEdgeInstallation installation = getOpenEdgeInstallation(oeAxis);
		// This is executed on the master server, so we can't use getVersionNumber from the Node specific OpenEdgeInstallation
		// However, it should be expected to be in the same major version !
		final String majorVersion = installation.getVersionNumber(); 
		if (installation != null) {
			installation = installation.forNode(Computer.currentComputer().getNode(), listener);
		}

		// Add DLC variable and modify PATH
		final OpenEdgeInstallation install = installation;
		return new Environment() {
			@Override
			public void buildEnvVars(Map<String, String> env) {
				if (install != null) {
					EnvVars envVars = new EnvVars();
					install.buildEnvVars(envVars);
					envVars.put("OE_MAJOR_VERSION", majorVersion);
					env.putAll(envVars);
				}
			}
		};
	}

	private OpenEdgeInstallation getOpenEdgeInstallation(String str) {
		for (OpenEdgeInstallation i : ((DescriptorImpl) getDescriptor()).getInstallations()) {
			if (i.getName().equals(str)) {
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
			return "Use OpenEdge version from matrix axis";
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			// TODO Better solution would be to return false only if an OpenEdge axis is configured
			if (item instanceof MatrixProject)
				return true;

			return false;
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
