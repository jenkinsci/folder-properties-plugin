package com.mig82.folders.global;

import com.mig82.folders.Messages;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundSetter;

@Extension
public class Config extends GlobalConfiguration {
    private boolean enabledGlobally;

    @NonNull
    public static Config get() {
        Config c = GlobalConfiguration.all().get(Config.class);
        if (c == null) {
            throw new IllegalStateException("config not found");
        }
        return c;
    }

    public Config() {
        load();
    }

    public boolean isEnabledGlobally() {
        return enabledGlobally;
    }

    @DataBoundSetter
    public void setEnabledGlobally(boolean value) {
        this.enabledGlobally = value;
        save();
    }

    @Override
    @NonNull
    public String getDisplayName() {
        return Messages.display_global_configuration();
    }
}
