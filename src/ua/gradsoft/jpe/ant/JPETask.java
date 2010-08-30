/*
 * JPETask.java
 *
 * Created on September 20, 2007, 3:43 PM
 *
 */

package ua.gradsoft.jpe.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;
import ua.gradsoft.jpe.CompileTimeConstants;
import ua.gradsoft.jpe.Configuration;
import ua.gradsoft.jpe.JPEConfigurationException;
import ua.gradsoft.jpe.JPEProcessingException;
import ua.gradsoft.jpe.Main;
import ua.gradsoft.termware.TermWare;

/**
 *Task for call JPE from ant scripts.
 * @author rssh
 */
public class JPETask extends Task {
    
    /** Creates a new instance of JPETask */
    public JPETask() {
    }
    
    public void setJpehome(String value) {
        configuration_.setJPEHome(value);
    }
    
    public void setInput(String value) {
        configuration_.setInputDir(value);
    }
    
    public void  setOutput(String value) {
        configuration_.setOutputDir(value);
    }
    
    public void  setCreateOutputDir(boolean value) {
        configuration_.setCreateOutputDir(value);
    }
    
    public void addConfiguredIncludedirs(DirSet dirSet) {
        dirSet.setProject(this.getProject());
        if (configuration_.getIncludeDirs()==null) {
            configuration_.setIncludeDirs(new ArrayList<String>());
        }
        if (dirSet.getDir(getProject())==null) {
            System.err.print("dirSet.getDir()==null ");
        }
        configuration_.getIncludeDirs().add(dirSet.getDir(getProject()).getAbsolutePath());
    }
    
    public void addConfiguredIncludejars(Path path) {
        configuration_.getIncludeJars().addAll(Arrays.asList(path.list()));
    }
    
    public void setCtclass(String ctclass)
    {
        configuration_.setCompileTimeClassname(ctclass);
    }
       
    public void setDebugLevel(int debugLevel)
    {
        configuration_.setDebugLevel(debugLevel);
    }
    
    public void setSilent(boolean silent)
    {
        configuration_.setSilent(silent);
    }
    
    public static class NVPair {
        private String name_;
        private String value_;
        
        public String getName() {return name_;}
        public void setName(String name) { name_=name; }
        
        public String getValue() { return value_; }
        public void setValue(String value) { value_=value; }
        
        public String toString() {
            return "("+name_+","+value_+")"; }
    }
    
    public void addConfiguredCtconstant(NVPair ctconstant) {
        if (CompileTimeConstants.DEMO) {
            int cs = configuration_.getCompileTimeProperties().size();
            if (cs >= CompileTimeConstants.DEMO_MAX_CONFIG_VALUES) {
                System.err.println("Only two compile time constants is allowed in demo version");
                System.err.print("ignore "+ctconstant.toString());
            }else{
                configuration_.getCompileTimeProperties().put(ctconstant.getName(),ctconstant.getValue());
            }
        }else{
            configuration_.getCompileTimeProperties().put(ctconstant.getName(),ctconstant.getValue());
        }
    }
    
    public void setTransformation(String transformation) {
        configuration_.setTransformationName(transformation);
    }
    
    public void setFork(boolean fork) {
        fork_=fork; }
    
    public void setFailOnError(boolean value) {
        failOnError_=value;
    }
    
    public Commandline.Argument  createJvmarg() {
        return cmd.createVmArgument();
    }
    
    
    public static class ClassnameHolder
    {
        private String classname_;
        
        public String getClassname()
        { return classname_; }
        public void setClassname(String classname)
        { classname_=classname; }
    }
    
    public void  addConfiguredDisable(ClassnameHolder value)
    {
        configuration_.addDisabledClass(value.getClassname());
    }
    
    
    public static class ClassnamepatternHolder
    {
         private String classnamepattern_;
         
        public String getClassnamepattern()
        { return classnamepattern_; }
        public void setClassnamepattern(String classnamepattern)
        { classnamepattern_=classnamepattern; }
         
    }
    
    public static class DevirtualizationHolder
    {
        public void setEnabled(boolean enabled)
        { enabled_=enabled; }
        
        public boolean isEnabled()
        { return enabled_; }
        
        public void addConfiguredExcept(ClassnamepatternHolder except)
        { excepts_.add(except.getClassnamepattern()); }
        
        public List<String> getExcepts()
        { return excepts_; }
        
        private boolean       enabled_=false;
        private LinkedList<String>  excepts_=new LinkedList<String>();
    }
    
    public void addConfiguredDevirtualization(DevirtualizationHolder holder)
    {
       configuration_.setDevirtualizationEnabled(holder.isEnabled());
       configuration_.getNonFinalClassPatterns().addAll(holder.getExcepts());
    }
    
    public static class EliminateUnreachableHolder
    {
     
        public void setEnabled(boolean enabled)
        { enabled_=enabled; }
        
        public boolean getEnabled()
        { return enabled_; }
        
        public void addConfiguredReachable(ClassnamepatternHolder reachable)
        {
            reachables_.add(reachable.getClassnamepattern());
        }
        
        public List<String> getReachables()
        { return reachables_; }
        
        private boolean enabled_;
        private LinkedList<String> reachables_ = new LinkedList<String>();
        
    }
    
    public void addConfiguredEliminateUnreachable(EliminateUnreachableHolder holder)
    {
        configuration_.setUnreachableCodeEliminationEnabled(holder.getEnabled());
        configuration_.getReachableClassPatterns().addAll(holder.getReachables());
    }
    
    public void execute() throws BuildException {
        if (failOnError_) {
            try {
                executeWithError();
            }catch(BuildException ex){
                log("Caught exception :"+ex.getMessage(),Project.MSG_WARN);
                Throwable ex1 = ex;
                while(ex1!=null) {
                    StackTraceElement[] stack = ex1.getStackTrace();
                    for(int i=0; i<stack.length; ++i) {
                        log(stack[i].getClassName()+"."+stack[i].getMethodName()+" at "+stack[i].getFileName()+","+stack[i].getLineNumber(),Project.MSG_VERBOSE);
                    }
                    ex1 = ex1.getCause();
                    if (ex1!=null) {
                        log("Caused by "+ex1.getMessage(),Project.MSG_VERBOSE);
                    }
                }
            }
        }else{
            executeWithError();
        }
    }
    
    public void executeWithError() throws BuildException {
        if (fork_) {
            executeFork();
        }else{
            executeInprocess();
        }
    }
    
    public void executeInprocess() throws BuildException {
        Main main = new Main();
        try {            
          if (!CompileTimeConstants.IN_IDE) {
            try{  
              TermWare.getInstance().setTermLoaderClassName(JPETermLoader.class.getName());              
            }catch(ClassNotFoundException ex){
                throw new BuildException(ex);
            }catch(InstantiationException ex){
                throw new BuildException(ex);
            }catch(IllegalAccessException ex){
                throw new BuildException(ex);
            }
          }  
          main.init(configuration_);
          main.run();
        }catch(JPEConfigurationException ex){
            throw new BuildException(ex);
        }catch(JPEProcessingException ex){
            throw new BuildException(ex);
        }
    }
    
    public void executeFork() throws BuildException {
        cmd.createArgument().setValue("--input-dir");
        cmd.createArgument().setValue(configuration_.getInputDir());
        for(String includeDir: configuration_.getIncludeDirs()){
            cmd.createArgument().setValue("--include-dir");
            cmd.createArgument().setValue(includeDir);
        }
        for(String includeJar: configuration_.getIncludeJars()){
            cmd.createArgument().setValue("--include-jar");
            cmd.createArgument().setValue(includeJar);
        }
        cmd.createArgument().setValue("--output-dir");
        cmd.createArgument().setValue(configuration_.getOutputDir());
        
        // --input-file skip
        if (configuration_.getJPEHome()!=null) {
          cmd.createArgument().setValue("--jpehome");
          cmd.createArgument().setValue(configuration_.getJPEHome());
        }
        
        cmd.createArgument().setValue("--transformation");
        cmd.createArgument().setValue(configuration_.getTransformationName());
        
        for(Map.Entry<String,String> e: configuration_.getCompileTimeProperties().entrySet()) {
            cmd.createArgument().setValue("--value-pair");
            cmd.createArgument().setValue(e.getKey());
            cmd.createArgument().setValue(e.getValue());
        }
        
        for(String classname : configuration_.getDisabledClasses()) {
            cmd.createArgument().setValue("--disable-class");
            cmd.createArgument().setValue(classname);
        }
        
        if (configuration_.isCreateOutputDir()) {
            cmd.createArgument().setValue("--create-output-dir");
        }
        
        if (configuration_.getDebugLevel() > 0) {
            cmd.createArgument().setValue("--debug-level");
            cmd.createArgument().setValue(Integer.toString(configuration_.getDebugLevel()));
        }
        
        if (configuration_.isSilent()) {
            cmd.createArgument().setValue("--silent");
        }
        
        Path classpath=cmd.createClasspath(getProject());
        String libdir = configuration_.getJPEHome()+File.separator+"lib"+File.separator;
        File f = new File(libdir);
        String libs[]=f.list();
        for(String libname:libs) {
           classpath.createPathElement().setPath(libdir+libname);
        }
        if (CompileTimeConstants.IN_IDE)       {
            classpath.createPathElement().setPath(configuration_.getJPEHome()+File.separator+"dist"+File.separator+"JPE.jar");
        }
        
        if (configuration_.isDevirtualizationEnabled()) {
            cmd.createArgument().setValue("--enable-devirtualization");
            for(String s: configuration_.getNonFinalClassPatterns()) {
                cmd.createArgument().setValue("--devirtualization-except");
                cmd.createArgument().setValue(s);
            }
        }
        
        if (configuration_.isUnreachableCodeEliminationEnabled()) {
            cmd.createArgument().setValue("--eliminate-unreachable");
            for(String s: configuration_.getReachableClassPatterns()) {
                cmd.createArgument().setValue("--reachable");
                cmd.createArgument().setValue(s);
            }
        }
        
        cmd.setClassname("ua.gradsoft.jpe.Main");
        
        
       // System.out.print("args=");
       // String[] args=cmd.getCommandline();
       // for(String arg:args) {
       //     System.out.print(arg);
       //     System.out.print(' ');
       // }
       // System.out.println();
        
        ExecuteWatchdog watchdog = null; // TODO: add timeout and rral watchdog.
        Execute execute = new Execute(new LogStreamHandler(this,Project.MSG_INFO,Project.MSG_WARN),watchdog);
        execute.setCommandline(cmd.getCommandline());
        int retcode=0;
        try {
            retcode = execute.execute();
        }catch(IOException ex){
            throw new BuildException("fork failed",ex,getLocation());
        }
        if (failOnError_ && Execute.isFailure(retcode)) {
            throw new BuildException("fork process return with error",null,getLocation());
        }
        
        
    }
    
    private Configuration configuration_ = new Configuration();
    private boolean fork_=false;
    private boolean failOnError_=false;
    private CommandlineJava cmd = new CommandlineJava();
}
