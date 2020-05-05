package com.mathworks.ci;
/**
 * Copyright 2019-2020 The MathWorks, Inc.
 * 
 * Tester builder for RunMatlabTestsBuilder.
 * 
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;

public class RunMatlabTestsBuilderTester extends RunMatlabTestsBuilder {

    private TapArtifact tapChkBx;
    private JunitArtifact junitChkBx;
    private CoberturaArtifact coberturaChkBx;
    private StmResultsArtifact stmResultsChkBx;
    private ModelCovArtifact modelCoverageChkBx;
    private PdfArtifact pdfReportChkBx;
    private EnvVars env;
    private int buildResult;
    private MatlabReleaseInfo matlabRel;
    private String matlabroot;
    private String commandParameter;
    private String matlabExecutorPath;



    public RunMatlabTestsBuilderTester(String matlabExecutorPath, String customTestPointArgument) {
        super();
        this.commandParameter = customTestPointArgument;
        this.matlabExecutorPath = matlabExecutorPath;
    }



    // Getter and Setters to access local members


    @DataBoundSetter
    public void setTapChkBx(TapArtifact tapChkBx) {
        this.tapChkBx = tapChkBx;
    }

    @DataBoundSetter
    public void setJunitChkBx(JunitArtifact junitChkBx) {
        this.junitChkBx = junitChkBx;
    }

    @DataBoundSetter
    public void setCoberturaChkBx(CoberturaArtifact coberturaChkBx) {
        this.coberturaChkBx = coberturaChkBx;
    }

    @DataBoundSetter
    public void setStmResultsChkBx(StmResultsArtifact stmResultsChkBx) {
        this.stmResultsChkBx = stmResultsChkBx;
    }

    @DataBoundSetter
    public void setModelCoverageChkBx(ModelCovArtifact modelCoverageChkBx) {
        this.modelCoverageChkBx = modelCoverageChkBx;
    }

    @DataBoundSetter
    public void setPdfReportChkBx(PdfArtifact pdfReportChkBx) {
        this.pdfReportChkBx = pdfReportChkBx;
    }

    public TapArtifact getTapChkBx() {
        return tapChkBx;
    }

    public JunitArtifact getJunitChkBx() {
        return junitChkBx;
    }

    public CoberturaArtifact getCoberturaChkBx() {
        return coberturaChkBx;
    }

    public StmResultsArtifact getStmResultsChkBx() {
        return stmResultsChkBx;
    }

    public ModelCovArtifact getModelCoverageChkBx() {
        return modelCoverageChkBx;
    }

    public PdfArtifact getPdfReportChkBx() {
        return pdfReportChkBx;
    }

    private void setEnv(EnvVars env) {
        this.env = env;
    }

    public void geetEnv(EnvVars env) {
        this.env = env;
    }

    public String getMatlabRoot() {
        return this.matlabroot;
    }

    @Extension
    public static class Descriptor extends BuildStepDescriptor<Builder> {
        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }

        /*
         * This is to identify which project type in jenkins this should be applicable.(non-Javadoc)
         * 
         * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
         * 
         * if it returns true then this build step will be applicable for all project type.
         */
        @Override
        public boolean isApplicable(
                @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobtype) {
            return true;
        }
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace,
            @Nonnull Launcher launcher, @Nonnull TaskListener listener)
            throws InterruptedException, IOException {
        // Set the environment variable specific to the this build
        setEnv(build.getEnvironment(listener));

        this.matlabroot = this.env.get("matlabroot");
        FilePath nodeSpecificMatlabRoot = new FilePath(launcher.getChannel(), matlabroot);
        matlabRel = new MatlabReleaseInfo(nodeSpecificMatlabRoot);

        buildResult = execCommand(workspace, launcher, listener);
        if (buildResult != 0) {
            build.setResult(Result.FAILURE);
        }
    }

    public int execCommand(FilePath workspace, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException {
        ProcStarter matlabLauncher;
        try {
            matlabLauncher = launcher.launch().pwd(workspace).envs(this.env);
            if (matlabRel.verLessThan(MatlabBuilderConstants.BASE_MATLAB_VERSION_BATCH_SUPPORT)) {
                ListenerLogDecorator outStream = new ListenerLogDecorator(listener);
                matlabLauncher = matlabLauncher.cmds(testMatlabCommand()).stderr(outStream);
            } else {
                matlabLauncher = matlabLauncher.cmds(testMatlabCommand()).stdout(listener);
            }

        } catch (Exception e) {
            listener.getLogger().println(e.getMessage());
            return 1;
        }
        return matlabLauncher.join();
    }

    private List<String> testMatlabCommand() {
        List<String> matlabDefaultArgs = new ArrayList<String>();
        matlabDefaultArgs.add(this.matlabExecutorPath);
        matlabDefaultArgs.add(this.commandParameter);
        return matlabDefaultArgs;
    }


}