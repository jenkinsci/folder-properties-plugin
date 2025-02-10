package com.mig82.folders.properties;

import com.mig82.folders.Messages;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * A class that holds a String property for a folder.
 *
 * @author Miguelangel Fernandez Mendoza
 */
public class StringProperty extends AbstractDescribableImpl<StringProperty> {

    private static final Logger LOGGER = Logger.getLogger(StringProperty.class.getName());

    private String key;
    private String value;

    @DataBoundConstructor
    public StringProperty(String key, String value) {
        this.setKey(key);
        this.setValue(value);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<StringProperty> {

        @Override
        public String getDisplayName() {

            return Messages.display_string_property();
        }

        /**
         * Checks that the key is not blank.
         */
        public FormValidation doValidate(@QueryParameter String key, @QueryParameter String value) {

            if (key == null || key.trim().isEmpty()) {
                return FormValidation.error(Messages.string_property_name_validation());
            }
            return FormValidation.ok(Messages.string_property_validation_success());
        }
    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + ": {\n" + "\tkey: '"
                + this.getKey() + "'\n" + "\tvalue: '"
                + this.getValue() + "'\n" + "}";
    }
}
