package com.mig82.folders;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.mig82.folders.properties.FolderProperties;
import com.mig82.folders.properties.StringProperty;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import java.io.IOException;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FolderPropertiesTest {

    private static JenkinsRule r;
    private static Folder f;

    @BeforeAll
    static void setUp(JenkinsRule rule) throws IOException {
        r = rule;
        // Create a top level parent folder to test with.
        f = r.jenkins.createProject(Folder.class, "f");

        // Add a couple of properties to test with.
        FolderProperties<?> properties = new FolderProperties<>();
        properties.setProperties(
                new StringProperty[] {new StringProperty("key1", "value1"), new StringProperty("key2", "value2")});
        f.addProperty(properties);
    }

    @Test
    void testFreestyle() throws Exception {
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
    void testFreestyleInSubFolder() throws Exception {
        // Create a subfolder.
        Folder sub = f.createProject(Folder.class, "sub-1");

        // Add a property in the subfolder that overrides another in the parent folder.
        FolderProperties<?> properties = new FolderProperties<>();
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
    void testPipelineInNode(TestInfo info) throws Exception {
        // Create a pipeline job which uses the properties from its parent folder.
        WorkflowJob p = PipelineTestHelper.createJob(
                f, "p-" + info.getTestMethod().orElseThrow().getName(), """
                node {
                  wrap([$class: 'ParentFolderBuildWrapper']) {
                    echo("key1: ${env.key1}")
                  }
                }
                """);

        // Run the build
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check the logs
        r.assertLogContains("key1: value1", b);
    }

    @Test
    void testPipelineInNodeNoWrap(TestInfo info) throws Exception {
        // Create a pipeline job which uses the properties from its parent folder.
        WorkflowJob p = PipelineTestHelper.createJob(
                f, "p-" + info.getTestMethod().orElseThrow().getName(), """
                node {
                  withFolderProperties {
                    echo("key1: ${env.key1}")
                  }
                }
                """);

        // Run the build
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check the logs
        r.assertLogContains("key1: value1", b);
    }

    @Test
    void testPipelineDeclarative(TestInfo info) throws Exception {
        // Create a declarative pipeline job which uses the properties from its parent folder.
        WorkflowJob p = PipelineTestHelper.createJob(
                f, "p-" + info.getTestMethod().orElseThrow().getName(), """
                pipeline {
                  agent any
                  stages {
                    stage('Report folder property key1') {
                      steps {
                        withFolderProperties {
                          echo "key1: ${env.key1}"
                        }
                      }
                    }
                  }
                }
                """);

        // Run the build
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check the logs
        r.assertLogContains("key1: value1", b);
    }

    @Test
    void testPipelineOutNode(TestInfo info) throws Exception {
        // Create a pipeline job which uses the properties from its parent folder.
        WorkflowJob p = PipelineTestHelper.createJob(
                f, "p-" + info.getTestMethod().orElseThrow().getName(), """
                withFolderProperties {
                  echo("key1: ${env.key1}")
                }
                """);

        // Run the build
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check the logs
        r.assertLogContains("key1: value1", b);
    }

    @Test
    void testPipelineInSubFolder(TestInfo info) throws Exception {
        // Create a subfolder.
        Folder sub = f.createProject(Folder.class, "sub-2");

        // Add a property in the subfolder that overrides another in the parent folder.
        FolderProperties<?> properties = new FolderProperties<>();
        properties.setProperties(new StringProperty[] {new StringProperty("key1", "override")});
        sub.addProperty(properties);

        // Create a pipeline job inside the subfolder which uses the properties from its parent folder.
        WorkflowJob p = PipelineTestHelper.createJob(
                sub, "p-" + info.getTestMethod().orElseThrow().getName(), """
                node {
                  wrap([$class: 'ParentFolderBuildWrapper']){
                    echo("key1: ${env.key1}")
                    echo("key2: ${env.key2}")
                  }
                }
                """);

        // Run the build
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check the logs
        r.assertLogContains("key1: override", b);
        r.assertLogContains("key2: value2", b);
    }

    @Test
    void testPipelineOverrideEnv(TestInfo info) throws Exception {
        // Create a pipeline job which uses the properties from its parent folder.
        WorkflowJob p = PipelineTestHelper.createJob(
                f, "p-" + info.getTestMethod().orElseThrow().getName(), """
                withEnv(['key1=old']) {
                  node {
                    wrap([$class: 'ParentFolderBuildWrapper']){
                      echo("key1: ${env.key1}")
                    }
                  }
                }
                """);

        // Run the build
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

        // Check the logs
        r.assertLogContains("key1: value1", b);
    }
}
