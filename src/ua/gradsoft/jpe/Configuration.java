/*
 * Configuration.java
 *
 *
 * Copyright (c) 2006-2010 GradSoft  Ukraine
 * All Rights Reserved
 */


package ua.gradsoft.jpe;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *Class which store JPE Configuration.
 */
public class Configuration implements Serializable {
    
    public void init(String[] args) throws JPEConfigurationException
    {
        int nPairs=0;
        String hashFileName=null;
        for(int i=0;i<args.length; ++i) {
            if (args[i].equals("--input-dir")) {
                if (i==args.length-1) {
                    throw new JPEConfigurationException("option --input-dir must have argument");
                }
                inputDir_=args[i+1];
                ++i;
            }else if(args[i].equals("--include-dir")) {
                if (i==args.length-1) {
                    throw new JPEConfigurationException("option --input-dir must have argument");
                }
                includeDirs_.add(args[i+1]);
                ++i;      
            }else if(args[i].equals("--include-jar")) {
                if (i==args.length-1) {
                    throw new JPEConfigurationException("option --input-jar must have argument");
                }
                includeJars_.add(args[i+1]);
                ++i;
            }else if(args[i].equals("--output-dir")) {
                if (i==args.length-1) {
                    throw new JPEConfigurationException("option --output-dir must have argument");
                }                
                outputDir_=args[i+1];
                ++i;
            }else if(args[i].equals("--ct-class")) {
                if (i==args.length-1) {
                    throw new JPEConfigurationException("option --ct-class must have argument");
                }                                
                compileTimeClass_=args[i+1];
                ++i;
            }else if(args[i].equals("--input-file")) {
                if (i==args.length-1) {
                    throw new JPEConfigurationException("option --input-file must have argument");
                }                                                
                hashFileName=args[i+1];
                ++i;
            }else if(args[i].equals("--jpehome")) {
                if (i==args.length-1) {
                    throw new JPEConfigurationException("option --jpehome must have argument");
                }
                jpeHome_=args[i+1];
                ++i;
            }else if(args[i].equals("--transformation")) {
                if (i==args.length-1) {
                    throw new JPEConfigurationException("option --transformation must have argument");
                }
                transformationName_=args[i+1];
                ++i;           
            }else if(args[i].equals("--disable-class")) {
                if (i==args.length-1) {
                    throw new JPEConfigurationException("option --disable-class must have argument");
                }                
                disabledClasses_.add(args[i+1]);
                ++i;
            }else if(args[i].equals("--dump")){
                dump_=true;
            }else if(args[i].equals("--silent")){
                isSilent_=true;
            }else if(args[i].equals("--debug-level")){
                if (i==args.length-1) {
                    throw new JPEConfigurationException("option --debug-level must have argument");
                }                
                try {
                  debugLevel_=Integer.parseInt(args[i+1]);
                }catch(NumberFormatException ex){
                    throw new JPEConfigurationException("option --debug-level must have integer argument",ex);
                }
                ++i;                
            }else if(args[i].equals("--value-pair")){
                if (i>=args.length-2) {
                    throw new JPEConfigurationException("option --value-pair must have two arguments");
                }                                                
                String key=args[++i];
                String value=args[++i];
                ++nPairs;
                if (CompileTimeConstants.DEMO) {
                    if (nPairs > CompileTimeConstants.DEMO_MAX_CONFIG_VALUES) {
                        System.err.println("Warning: Demo version supports only "+CompileTimeConstants.DEMO+" value pairs");
                        System.err.println("So ("+key+","+value+") is ignored");                        
                    }else{
                        compileTimeProperties_.put(key,value);
                    }
                }else{
                   compileTimeProperties_.put(key,value);
                }
            }else if(args[i].equals("--create-output-dir")){
                createOutputDir_=true;
            }else if(args[i].equals("--devirtualization-enable")){
                setDevirtualizationEnabled(true);            
            }else if(args[i].equals("--enable-devirtualization")){
                setDevirtualizationEnabled(true);            
            }else if(args[i].equals("--devirtualizarion-except")){
                if (i==args.length-1) {
                    throw new JPEConfigurationException("option --devirtualization-except must have argument");
                }                                
                addNonFinalClassPattern(args[i+1]);
                ++i;
            }else if(args[i].equals("--eliminate-unreachable")){
                unreachableCodeEliminationEnabled_=true;
            }else if(args[i].equals("--reachable")){
                if (i==args.length-1) {
                    throw new JPEConfigurationException("option --reachable must have argument");
                }                                
                addReachableClassPattern(args[i+1]);
                ++i;                
            }else{
                throw new JPEConfigurationException("Unknown option:"+args[i]);
            }
        }
        if (hashFileName!=null) {
            readHashFile(hashFileName);
        }
        if (inputDir_==null) {
            throw new JPEConfigurationException("option --input-dir is mandatory");            
        }
        if (outputDir_==null) {
            throw new JPEConfigurationException("option --output-dir is mandatory");            
        }
        //if (jpeHome_==null) {
        //    throw new JPEConfigurationException("option --jpe-home is mandatory");            
        //}
    }
    
    private void readHashFile(String hashFileName) throws JPEConfigurationException
    {
        throw new JPEConfigurationException("readHashFile is not implemented yet");
    }
    
    public String getInputDir()
    {
        return inputDir_;
    }
    
    public void  setInputDir(String inputDir)
    {
       inputDir_ = inputDir; 
    }        
    
    public String getOutputDir()
    {
        return outputDir_;
    }
    
    public void  setOutputDir(String outputDir)
    {
        outputDir_=outputDir;
    }
    
    public String getJPEHome()
    {
        return jpeHome_;
    }
    
    public void  setJPEHome(String jpeHome)
    {
       jpeHome_=jpeHome; 
    }
    
    public String getTransformationName()
    {
        return transformationName_;
    }
    
    public void setTransformationName(String transformationName)
    { 
        transformationName_=transformationName; 
    }
    
    public Set<String>  getDisabledClasses()
    {
        return disabledClasses_;
    }
    
    public boolean isDisabledClass(String classname)
    {
        return disabledClasses_.contains(classname);
    }
    
    public void addDisabledClass(String classname)
    {
        disabledClasses_.add(classname);
    }
    
    public boolean isDump()
    {
        return dump_;
    }
    
    public void setDump(boolean dump)
    {
        dump_=dump;
    }
    
    public boolean isCreateOutputDir()
    {
        return createOutputDir_;
    }
    
    public void setCreateOutputDir(boolean createOutputDir)
    {
       createOutputDir_=createOutputDir; 
    }
    
    public void addIncludeDir(String includeDir)
    {
        includeDirs_.add(includeDir);
    }
    
    public List<String>  getIncludeDirs()
    {
        return includeDirs_;
    }
        
    
    public void  setIncludeDirs(List<String> includeDirs)
    {
        includeDirs_=includeDirs;
    }
    
    public void addIncludeJar(String includeJar)
    {
        includeJars_.add(includeJar);
    }
    
    public List<String>  getIncludeJars()
    {
        return includeJars_;
    }
        
    
    public void  setIncludeJars(List<String> includeJars)
    {
        includeJars_=includeJars;
    }
    
    public String getCompileTimeClassname()
    {
        return compileTimeClass_;
    }
    
    public void setCompileTimeClassname(String classname)
    {
        compileTimeClass_=classname;
    }
    
    public Map<String,String>  getCompileTimeProperties()
    {
       return compileTimeProperties_; 
    }
    
    public boolean isDevirtualizationEnabled()
    { return devirtualizationEnabled_; }
    
    public void  setDevirtualizationEnabled(boolean value)
    {
        devirtualizationEnabled_=value;
    }
    
    public void addNonFinalClassPattern(String classpattern)
    {
        nonFinalClassPatterns_.add(classpattern);
    }
    
    public List<String>  getNonFinalClassPatterns()
    {
        return nonFinalClassPatterns_;
    }
    
    public boolean isUnreachableCodeEliminationEnabled()
    {
        return unreachableCodeEliminationEnabled_;
    }
    
    public void setUnreachableCodeEliminationEnabled(boolean value)
    {
        unreachableCodeEliminationEnabled_ = value;
    }
    
    public void addReachableClassPattern(String classpattern)
    {
        reachableClassPatterns_.add(classpattern);
    }
    
    public List<String>  getReachableClassPatterns()
    {
        return reachableClassPatterns_;
    }
    
    public int getDebugLevel()
    {
        return debugLevel_;
    }
    
    public void setDebugLevel(int debugLevel)
    { 
        debugLevel_=debugLevel;
    }
    
    public boolean isSilent()
    { return isSilent_; }
    
    public void setSilent(boolean isSilent)
    { isSilent_=isSilent; }
    
    
    private boolean isSilent_=false;
    private boolean dump_=false;
    private int     debugLevel_=0;
    private boolean createOutputDir_=false;    
    private String inputDir_=null;
    private String outputDir_=null;
    private List<String>  includeDirs_=new LinkedList<String>();
    private List<String>  includeJars_=new LinkedList<String>();
    private String compileTimeClass_="CompileTimeConstants";
    private String jpeHome_=null;
    private String transformationName_="JPE";  
    private HashMap<String,String> compileTimeProperties_=new HashMap<String,String>();
    private HashSet<String> disabledClasses_ = new HashSet<String>();
    
    // optimizations data.
    private boolean      devirtualizationEnabled_ = false;
    private List<String> nonFinalClassPatterns_ = new LinkedList<String>();
    
    private boolean      unreachableCodeEliminationEnabled_ = false;
    private List<String> reachableClassPatterns_ = new LinkedList<String>();
    
}
