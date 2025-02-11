package com.mig82.folders;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.mig82.folders.wrappers.ParentFolderBuildWrapper;
import hudson.Functions;
import hudson.model.FreeStyleProject;
import hudson.tasks.BatchFile;
import hudson.tasks.Shell;
import java.io.IOException;

public class FreestyleTestHelper {

    public static FreeStyleProject createJob(Folder parent, String name) throws IOException {
        // Create a freestyle job
        FreeStyleProject p = parent.createProject(FreeStyleProject.class, name);

        // Add the parent folder build wrapper.
        p.getBuildWrappersList().add(new ParentFolderBuildWrapper());

        return p;
    }

    public static void addEcho(FreeStyleProject p, String key) {
        p.getBuildersList()
                .add(
                        Functions.isWindows()
                                ? new BatchFile("echo " + key + ": %" + key + "%")
                                : new Shell("echo " + key + ": $" + key));
    }
}
