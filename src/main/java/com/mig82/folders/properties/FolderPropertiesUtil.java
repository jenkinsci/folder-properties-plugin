package com.mig82.folders.properties;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FolderPropertiesUtil {
    private static final Logger LOGGER = Logger.getLogger(FolderPropertiesUtil.class.getName());

    public static EnvVars loadFolderProperties(Job job) {

        LOGGER.log(Level.FINER, "1. Searching for folder properties in ancestors of: {0}\n", job.getDisplayName());

        ItemGroup parent = job.getParent();
        FolderProperties folderProperties;
        EnvVars envVars = new EnvVars();
        //Look in all the ancestors...
        while (parent != null) {

            if (parent instanceof AbstractFolder) {

                LOGGER.log(Level.FINEST, "2. Searching for folder properties in: {0}\n", parent.getDisplayName());

                AbstractFolder folder = (AbstractFolder) parent;
                folderProperties = (FolderProperties) folder.getProperties().get(FolderProperties.class);
                if (folderProperties != null) {

                    StringProperty[] newlyFoundProperties = folderProperties.getProperties();
                    LOGGER.log(Level.FINER, "3. Found {0} folder properties in {1}\n", new Object[]{
                            newlyFoundProperties.length,
                            parent.getDisplayName()
                    });

                    //If we find folder project properties on this parent, we add all to the context.
                    for (StringProperty property : newlyFoundProperties) {
                        //Only add the property if it has not been already defined in a sub-folder.
                        if (envVars.getEnv().get(property.getKey()) == null) {
                            LOGGER.log(Level.FINEST, "4. Adding ({0}, {1}) to the context env", new Object[]{
                                    property.getKey(),
                                    property.getValue()
                            });
                            envVars.env(property.getKey(), property.getValue());
                        } else {
                            LOGGER.log(Level.FINEST, "4. Will not add duplicate property {0} to the context env", new Object[]{
                                    property.getKey()
                            });
                        }
                    }
                    LOGGER.log(Level.FINEST, "5. Context env: {0}", envVars.getEnv().toString());
                }
            } else if (parent instanceof Jenkins) {
                LOGGER.log(Level.FINEST, "2. Reached Jenkins root. Stopping search\n");
            } else {
                LOGGER.log(Level.WARNING, "2. Unknown parent type: {0} of class {1}\n", new Object[]{
                        parent.getDisplayName(),
                        parent.getClass().getName()
                });
            }

            //In the next iteration we want to search for the parent of this parent.
            if (parent instanceof Item) {
                parent = ((Item) parent).getParent();
            } else {
                parent = null;
            }
        }
        LOGGER.log(Level.FINE, "6. Context env is: {0}", envVars.getEnv().toString());
        return envVars;
    }


    public static final class EnvVars {
        private final Map<String, String> env = new HashMap<String, String>();

        public @Nonnull
        Map<String, String> getEnv() {
            return env;
        }

        public void env(String key, String value) {
            if (env.containsKey(key)) {
                throw new IllegalStateException("just one binding for " + key);
            }
            env.put(key, value);
        }


    }
}
