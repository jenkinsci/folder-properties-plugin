package com.mig82.folders.properties;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import hudson.EnvVars;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import jenkins.model.Jenkins;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A PropertiesLoader which can be used in both Freestyle Job build wrapper and custom pipeline step.
 * The loader loads StringProperty from current job folder to its ancient recursively.
 *
 * @author Miguelangel Fernandez Mendoza and Gong Yi
 */
public class PropertiesLoader {
	private static final Logger LOGGER = Logger.getLogger(PropertiesLoader.class.getName());

	public static EnvVars loadFolderProperties(Job<?,?> job) {
		LOGGER.log(Level.FINER, "1. Searching for folder properties in ancestors of: {0}\n", job.getDisplayName());
		ItemGroup<?> parent = job.getParent();
		final EnvVars envVars = new EnvVars();
		//Look in all the ancestors...
		while (parent != null) {
			if (parent instanceof AbstractFolder<?>) {
				LOGGER.log(Level.FINEST, "2. Searching for folder properties in: {0}\n", parent.getDisplayName());
				final AbstractFolder<?> folder = (AbstractFolder<?>) parent;
				final FolderProperties<?> folderProperties = (FolderProperties<?>) folder.getProperties().get(FolderProperties.class);
				if (folderProperties != null) {
					StringProperty[] newlyFoundProperties = folderProperties.getProperties();
					LOGGER.log(Level.FINER, "3. Found {0} folder properties in {1}\n", new Object[]{
							newlyFoundProperties.length,
							parent.getDisplayName()
					});
					//If we find folder project properties on this parent, we add all to the context.
					for (StringProperty property : newlyFoundProperties) {
						//Only add the property if it has not been already defined in a sub-folder.
						if (envVars.get(property.getKey()) == null) {
							LOGGER.log(Level.FINEST, "4. Adding ({0}, {1}) to the context env", new Object[]{
									property.getKey(),
									property.getValue()
							});
							envVars.put(property.getKey(), property.getValue());
						} else {
							LOGGER.log(Level.FINEST, "4. Will not add duplicate property {0} to the context env", new Object[]{
									property.getKey()
							});
						}
					}
					LOGGER.log(Level.FINEST, "5. Context env: {0}", envVars.toString());
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
		LOGGER.log(Level.FINE, "6. Context env is: {0}", envVars.toString());
		return envVars;
	}

}
