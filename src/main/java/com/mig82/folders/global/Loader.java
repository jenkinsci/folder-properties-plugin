package com.mig82.folders.global;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.mig82.folders.properties.FolderProperties;
import com.mig82.folders.properties.StringProperty;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class Loader extends EnvironmentContributor {
    private static final Logger LOGGER = Logger.getLogger(Loader.class.getName());

    private static void applyProperties(@NonNull EnvVars envs, @NonNull StringProperty[] properties) {
        for (var property : properties) {
            LOGGER.log(Level.FINEST, "Variable {0}: {1}", new String[] {property.getKey(), property.getValue()});
            envs.put(property.getKey(), property.getValue());
        }
    }

    private static void applyGroup(@NonNull EnvVars envs, @NonNull ItemGroup<?> group) {
        if (group instanceof AbstractFolder<?> folder) {
            applyGroup(envs, folder.getParent());
            LOGGER.log(Level.FINER, "Folder {0}", folder.getName());
            var props = folder.getProperties().get(FolderProperties.class);
            if (props != null) {
                Loader.applyProperties(envs, props.getProperties());
            }
        }
    }

    @Override
    public void buildEnvironmentFor(@NonNull Job job, @NonNull EnvVars envs, @NonNull TaskListener listener) {
        if (Config.get().isEnabledGlobally()) {
            LOGGER.log(Level.FINER, "About to apply folder properties");
            Loader.applyGroup(envs, job.getParent());
        } else {
            LOGGER.log(Level.FINER, "Not applying folder properties");
        }
    }
}
