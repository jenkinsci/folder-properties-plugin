package com.mig82.folders.properties;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import com.mig82.folders.Messages;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.util.CopyOnWriteList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ArrayUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * A class that holds an array of properties for a folder.
 *
 * @author Miguelangel Fernandez Mendoza
 */
public class FolderProperties<C extends AbstractFolder<?>> extends AbstractFolderProperty<AbstractFolder<?>> {

    private static final Logger LOGGER = Logger.getLogger(FolderProperties.class.getName());

    /**
     * The list properties held by the project folder.
     */
    private final CopyOnWriteList<StringProperty> properties = new CopyOnWriteList<StringProperty>();

    /**
     * Constructor.
     */
    @DataBoundConstructor
    public FolderProperties() {
        LOGGER.log(Level.FINER, "Instantiating new FolderProperties\n");
    }

    @Override
    public AbstractFolderProperty<?> reconfigure(StaplerRequest request, JSONObject formData)
            throws Descriptor.FormException {
        if (formData == null) {
            return null;
        }

        properties.replaceBy(request.bindJSONToList(StringProperty.class, formData.get("properties")));
        return this;
    }

    /**
     * Returns the properties added to the project folder.
     *
     * @return The array of properties added to the folder.
     */
    public StringProperty[] getProperties() {
        return properties.toArray(new StringProperty[0]);
    }

    /**
     * Adds a bunch of properties to the project folder.
     *
     * @param properties The array of properties to be added to the project folder.
     *
     */
    @DataBoundSetter
    public void setProperties(StringProperty[] properties) {
        LOGGER.log(Level.FINER, "FolderProperties.setProperties({0})\n", ArrayUtils.toString(properties));
        for (StringProperty property : properties) {
            this.properties.add(property);
        }
    }

    /*
     * The {@link AbstractFolder} object that owns this property.
     * This value will be set by the folder.
     * Derived classes can expect this value to be always set.
     *
    protected transient C owner;

    /*
     * Hook for performing post-initialization action.
     * @param owner the owner.
     *
    protected void setPropertyOwner(@NonNull C owner) {
        this.owner = owner;
    }*/

    /**
     * Descriptor class.
     */
    @Extension
    public static class DescriptorImpl extends AbstractFolderPropertyDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {

            return Messages.display_folder_properties();
        }
    }
}
