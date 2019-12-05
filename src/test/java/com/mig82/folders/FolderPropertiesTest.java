package com.mig82.folders;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.mig82.folders.properties.FolderProperties;
import com.mig82.folders.properties.StringProperty;
import com.mig82.folders.wrappers.ParentFolderBuildWrapper;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Shell;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import static org.junit.Assert.*;

public class FolderPropertiesTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();
    private Folder f;

    @Before
    public void setUp() throws IOException {
        f = r.jenkins.createProject(Folder.class, "f");
        FolderProperties properties=new FolderProperties();
        properties.setProperties(new StringProperty[]{new StringProperty("key1","value1")});
        f.addProperty(properties);
    }

    @Test
    public void testPipelineOutOfNode() throws Exception {
        WorkflowJob p = f.createProject(WorkflowJob.class, "p");
        String script="withFolderProperties{\n" +
                "    echo(\"key1: ${env.key1}\")\n" +
                "}";
        p.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("key1: value1", b);
    }

    @Test
    public void testPipelineInNode() throws Exception {
        WorkflowJob p = f.createProject(WorkflowJob.class, "p");
        String script="node {\n" +
                "\n" +
                "            withFolderProperties{\n" +
                "                echo(\"key1: ${env.key1}\")\n" +
                "            }\n" +
                "        }";
        p.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("key1: value1", b);
    }

    @Test
    public void testFreeStyleProject() throws Exception {

        FreeStyleProject p = f.createProject(FreeStyleProject.class, "p");
        p.getBuildWrappersList().add(new ParentFolderBuildWrapper());
        p.getBuildersList().add(new Shell("echo key1: $key1"));
        FreeStyleBuild b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("key1: value1", b);
    }

    @Test
    public void testSubFolder() throws Exception {
        Folder sub = f.createProject(Folder.class, "sub");
        FolderProperties properties=new FolderProperties();
        properties.setProperties(new StringProperty[]{new StringProperty("key1","override")});
        sub.addProperty(properties);

        WorkflowJob p = sub.createProject(WorkflowJob.class, "p");
        String script="node {\n" +
                "\n" +
                "            withFolderProperties{\n" +
                "                echo(\"key1: ${env.key1}\")\n" +
                "            }\n" +
                "        }";
        p.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("key1: override", b);
    }


    @Test
    public void testPipelineOverrideEnv() throws Exception {
        WorkflowJob p = f.createProject(WorkflowJob.class, "p");
        String script="withEnv(['key1=old']) {\n" +
                "    withFolderProperties{\n" +
                "        echo(\"key1: ${env.key1}\")\n" +
                "    }\n" +
                "}";
        p.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("key1: value1", b);
    }

}