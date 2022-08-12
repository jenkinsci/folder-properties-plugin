package com.mig82.folders.wrappers;

import com.mig82.folders.properties.PropertiesLoader;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.util.Map;

@Extension
public class ParentFolderEnvContributor extends EnvironmentContributor {

  @Override
  public void buildEnvironmentFor(@NonNull Job job, @NonNull EnvVars envs, @NonNull TaskListener listener) {
    final EnvVars overrideVars = PropertiesLoader.loadFolderProperties(job);
    for (Map.Entry<String, String> entry : overrideVars.entrySet()) {
      envs.override(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void buildEnvironmentFor(@NonNull Run run, @NonNull EnvVars envs, @NonNull TaskListener listener) {
    buildEnvironmentFor(run.getParent(), envs, listener);
  }
}