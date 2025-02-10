package com.mig82.folders.step;

import com.mig82.folders.properties.PropertiesLoader;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A custom pipeline step to retrieve folder properties
 * This step is able to run out of a node
 *
 * @author Miguelangel Fernandez Mendoza and Gong Yi
 */
public class FolderPropertiesStep extends Step implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(FolderPropertiesStep.class.getName());

    @DataBoundConstructor
    public FolderPropertiesStep() {}

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new Execution(stepContext, this);
    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private FolderPropertiesStep folderPropertiesStep;

        public Execution(StepContext context, FolderPropertiesStep folderPropertiesStep) {
            super(context);
            this.folderPropertiesStep = folderPropertiesStep;
        }

        @Override
        protected Void run() throws Exception {
            LOGGER.log(Level.FINER, "Run in 'withFolderProperties' custom pipeline step");
            Job job = getContext().get(Run.class).getParent();
            EnvVars envVars = PropertiesLoader.loadFolderProperties(job);
            BodyInvoker bodyInvoker = getContext().newBodyInvoker();
            if (!envVars.isEmpty()) {
                LOGGER.log(Level.FINER, "Find the folder properties");
                bodyInvoker.withContext(EnvironmentExpander.merge(
                        getContext().get(EnvironmentExpander.class), new ExpanderImpl(envVars)));
            }
            bodyInvoker.start().get();
            return null;
        }
    }

    private static final class ExpanderImpl extends EnvironmentExpander {
        private static final long serialVersionUID = 1;
        private final EnvVars overrides;

        ExpanderImpl(EnvVars overrides) {
            this.overrides = /* ensure serializability*/ new EnvVars(overrides);
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
