package com.mig82.folders.steps;

import com.mig82.folders.properties.FolderPropertiesUtil;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FolderPropertiesStep extends Step implements Serializable {

    @DataBoundConstructor
    public FolderPropertiesStep() {
    }

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new Execution(stepContext, this);
    }

    private static class Execution extends AbstractSynchronousNonBlockingStepExecution<Void> {
        private FolderPropertiesStep folderPropertiesStep;

        public Execution(StepContext context, FolderPropertiesStep folderPropertiesStep) {
            super(context);
            this.folderPropertiesStep = folderPropertiesStep;
        }

        @Override
        protected Void run() throws Exception {
            Job job = getContext().get(Run.class).getParent();
            FolderPropertiesUtil.EnvVars envVars = FolderPropertiesUtil.loadFolderProperties(job);
            BodyInvoker bodyInvoker = getContext().newBodyInvoker();

            if (!envVars.getEnv().isEmpty()) {
                bodyInvoker.withContext(EnvironmentExpander.merge(getContext().get(EnvironmentExpander.class), new ExpanderImpl(envVars.getEnv())));
            }
            bodyInvoker.start().get();

            return null;
        }
    }

    private static final class ExpanderImpl extends EnvironmentExpander {
        private static final long serialVersionUID = 1;
        private final Map<String, String> overrides;

        ExpanderImpl(Map<String, String> overrides) {
            this.overrides = /* ensure serializability*/ new HashMap<>(overrides);
        }

        @Override
        public void expand(EnvVars env) throws IOException, InterruptedException {
            // Distinct from EnvironmentExpander.constant since we are also expanding variables.
            env.overrideExpandingAll(overrides);
        }
    }
    @Extension
    public static class DescriptorImpl extends StepDescriptor {


        @Override
        public Set<Class<?>> getRequiredContext() {
            return Collections.<Class<?>>singleton(TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "withFolderProperties";
        }

        @Override
        public String getDisplayName() {
            return "A step to retrieve folder properties";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }

}
