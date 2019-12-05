package com.mig82.folders.wrappers;

import com.mig82.folders.Messages;
import com.mig82.folders.properties.FolderPropertiesUtil;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.tasks.SimpleBuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A build wrapper which exposes the properties defined for a folder to all the jobs contained inside it.
 * Freestyle jobs must opt into using this build wrapper and pipeline jobs can access these properties by using the
 * custom {@code withFolderProperties} step.
 *
 * @author Miguelangel Fernandez Mendoza
 */
public class ParentFolderBuildWrapper extends SimpleBuildWrapper {

    private static final Logger LOGGER = Logger.getLogger(ParentFolderBuildWrapper.class.getName());

    @DataBoundConstructor
    public ParentFolderBuildWrapper() {
    }

    //Add the properties from the parent ProjectFolder folder to the context of the job before it starts.
    @Override
    public void setUp(
            Context context,
            Run<?, ?> run,
            FilePath workspace,
            Launcher launcher,
            TaskListener listener,
            EnvVars initialEnvironment
    ) {

        Job job = run.getParent(); //The parent of the run is the Job itself.
        Map<String, String> env = context.getEnv();
        Map<String, String> envVars = FolderPropertiesUtil.loadFolderProperties(job).getEnv();
        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            String key = entry.getKey();
            if (!env.containsKey(key)) {
                env.put(key, entry.getValue());
            }
        }


        LOGGER.log(Level.FINE, "7. Context env is: {0}", context.getEnv().toString());
    }


    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.display_build_wrapper();
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {

            LOGGER.log(Level.FINER, "Folder build wrapper is applicable to: {0}\n", item.getDisplayName());
            return true;
        }
    }
}