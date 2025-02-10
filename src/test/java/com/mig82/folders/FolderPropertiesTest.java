package com.mig82.folders;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.mig82.folders.properties.FolderProperties;
import com.mig82.folders.properties.StringProperty;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import java.io.IOException;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class FolderPropertiesTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    private static Folder f;

    @BeforeClass
    public static void setUp() throws IOException {

        // Create a top level parent folder to test with.
        f = r.jenkins.createProject(Folder.class, "f");

        // Add a couple of properties to test with.
        FolderProperties properties = new FolderProperties();
        properties.setProperties(
                new StringProperty[] {new StringProperty("key1", "value1"), new StringProperty("key2", "value2")});
        f.addProperty(properties);
    }

    @Test
    public void testFreestyle() throws Exception {

        // Create a freestyle project which attempts to use props from parent folder.
        FreeStyleProject p = FreestyleTestHelper.createJob(f, "p-1");
        FreestyleTestHelper.addEcho(p, "key1");
        FreestyleTestHelper.addEcho(p, "key2");

        // Run the build.
        FreeStyleBuild b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check that both properties were accessible.
        r.assertLogContains("key1: value1", b);
        r.assertLogContains("key2: value2", b);
    }

    @Test
    public void testFreestyleInSubFolder() throws Exception {

        // Create a subfolder.
        Folder sub = f.createProject(Folder.class, "sub-1");

        // Add a property in the subfolder that overrides another in the parent folder.
        FolderProperties properties = new FolderProperties();
        properties.setProperties(new StringProperty[] {new StringProperty("key1", "override")});
        sub.addProperty(properties);

        // Create a freestyle project in the subfolder that attempts to use props from parent and grandparent.
        FreeStyleProject p = FreestyleTestHelper.createJob(sub, "p-2");
        FreestyleTestHelper.addEcho(p, "key1");
        FreestyleTestHelper.addEcho(p, "key2");

        // Run the build.
        FreeStyleBuild b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check that both properties were accessible.
        r.assertLogContains("key1: override", b);
        r.assertLogContains("key2: value2", b);
    }

    @Test
    public void testPipelineInNode() throws Exception {

        // Create a pipeline job which uses the properties from its parent folder.
        WorkflowJob p = PipelineTestHelper.createJob(
                f,
                "p-3",
                "node {\n" + "  wrap([$class: 'ParentFolderBuildWrapper']) {\n"
                        + "    echo(\"key1: ${env.key1}\")\n"
                        + "  }\n"
                        + "}");

        // Run the build
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check the logs
        r.assertLogContains("key1: value1", b);
    }

    @Test
    public void testPipelineInNodeNoWrap() throws Exception {

        // Create a pipeline job which uses the properties from its parent folder.
        WorkflowJob p = PipelineTestHelper.createJob(
                f,
                "p-4",
                "node {\n" + "  withFolderProperties {\n" + "    echo(\"key1: ${env.key1}\")\n" + "  }\n" + "}");

        // Run the build
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check the logs
        r.assertLogContains("key1: value1", b);
    }

    @Test
    public void testPipelineDeclarative() throws Exception {

        // Create a declarative pipeline job which uses the properties from its parent folder.
        WorkflowJob p = PipelineTestHelper.createJob(
                f,
                "p-5",
                "pipeline {\n" + "  agent any\n"
                        + "  stages {\n"
                        + "    stage('Report folder property key1') {\n"
                        + "      steps {\n"
                        + "        withFolderProperties {\n"
                        + "          echo \"key1: ${env.key1}\"\n"
                        + "        }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + "}\n");

        // Run the build
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check the logs
        r.assertLogContains("key1: value1", b);
    }

    @Test
    public void testPipelineOutNode() throws Exception {

        // Create a pipeline job which uses the properties from its parent folder.
        WorkflowJob p = PipelineTestHelper.createJob(
                f, "p-6", "withFolderProperties {\n" + "  echo(\"key1: ${env.key1}\")\n" + "}");

        // Run the build
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check the logs
        r.assertLogContains("key1: value1", b);
    }

    @Test
    public void testPipelineInSubFolder() throws Exception {

        // Create a subfolder.
        Folder sub = f.createProject(Folder.class, "sub-2");

        // Add a property in the subfolder that overrides another in the parent folder.
        FolderProperties properties = new FolderProperties();
        properties.setProperties(new StringProperty[] {new StringProperty("key1", "override")});
        sub.addProperty(properties);

        // Create a pipeline job inside the subfolder which uses the properties from its parent folder.
        WorkflowJob p = PipelineTestHelper.createJob(
                sub,
                "p-7",
                "node {\n" + "  wrap([$class: 'ParentFolderBuildWrapper']){\n"
                        + "    echo(\"key1: ${env.key1}\")\n"
                        + "    echo(\"key2: ${env.key2}\")\n"
                        + "  }\n"
                        + "}");

        // Run the build
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check the logs
        r.assertLogContains("key1: override", b);
        r.assertLogContains("key2: value2", b);
    }

    @Test
    public void testPipelineOverrideEnv() throws Exception {

        // Create a pipeline job which uses the properties from its parent folder.
        WorkflowJob p = PipelineTestHelper.createJob(
                f,
                "p-8",
                "withEnv(['key1=old']) {\n" + "  node {\n"
                        + "    wrap([$class: 'ParentFolderBuildWrapper']){\n"
                        + "      echo(\"key1: ${env.key1}\")\n"
                        + "    }\n"
                        + "  }\n"
                        + "}");

        // Run the build
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check the logs
        r.assertLogContains("key1: value1", b);
    }
}
