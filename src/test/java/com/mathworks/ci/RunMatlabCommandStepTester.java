package com.mathworks.ci;

import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

public class RunMatlabCommandStepTester extends RunMatlabCommandStep {
    @DataBoundConstructor
    public RunMatlabCommandStepTester(String matlabCommand) {
        super(matlabCommand);
    }
    
    @Override
    public StepExecution start(StepContext context) throws Exception {
        
        return new TestStepExecution(context,this.getMatlabCommand(), false);
    }
    
    @Extension
    public static class CommandStepTestDescriptor extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(TaskListener.class, FilePath.class, Launcher.class,
                    EnvVars.class, Run.class);
        }

        @Override
        public String getFunctionName() {
            return "testMATLABCommand";
        }
    }

}
