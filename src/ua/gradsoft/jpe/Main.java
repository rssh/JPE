/*
 * Main.java
 *
 *
 * Copyright (c) 2006-2010 GradSoft  Ukraine
 * http://www.gradsoft.ua
 * All Rights Reserved
 */


package ua.gradsoft.jpe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import ua.gradsoft.javachecker.ConfigException;
import ua.gradsoft.javachecker.EntityNotFoundException;
import ua.gradsoft.javachecker.FileAndLine;
import ua.gradsoft.javachecker.JUtils;
import ua.gradsoft.javachecker.JavaCheckerFacade;
import ua.gradsoft.javachecker.models.AnalyzedUnitRef;
import ua.gradsoft.javachecker.models.AnalyzedUnitType;
import ua.gradsoft.javachecker.models.InvalidJavaTermException;
import ua.gradsoft.javachecker.models.JavaCompilationUnitModel;
import ua.gradsoft.javachecker.models.JavaPackageModel;
import ua.gradsoft.javachecker.models.JavaTermTypeAbstractModel;
import ua.gradsoft.javachecker.models.JavaTypeModel;
import ua.gradsoft.javachecker.models.JavaUnitModel;
import ua.gradsoft.jpe.optimizations.MarkNumberOfChildsInSuper;
import ua.gradsoft.termware.IEnv;
import ua.gradsoft.termware.Term;
import ua.gradsoft.termware.TermHelper;
import ua.gradsoft.termware.TermSystem;
import ua.gradsoft.termware.TermWare;
import ua.gradsoft.termware.TermWareException;
import ua.gradsoft.termware.envs.SystemEnv;

/**
 *
 * @author Ruslan Shevchenko
 */
public class Main {
    
    public static void main(String args[]) {
        Main main=new Main();
        try{
            main.init(args);
            main.run();
        }catch(JPEConfigurationException ex){
            System.err.print("exception during initialization");
            ex.printStackTrace();
        }catch(JPEProcessingException ex){
            System.err.print("exception during processing");
            ex.printStackTrace();
        }
    }
    
    public void init(String[] args) throws JPEConfigurationException {
        configuration_.init(args);
        init(configuration_);
    }
    
    public void init(Configuration configuration) throws JPEConfigurationException {
        configuration_=configuration;
        IEnv env=new SystemEnv();
        TermWare.getInstance().setEnv(env);
        if (configuration_.getJPEHome()!=null) {
           JavaCheckerFacade.setHome(configuration_.getJPEHome());    
        }
        JavaCheckerFacade.setHomeRequired(false);
        JavaCheckerFacade.setMandatoryCheckersLoading(false);
        JavaCheckerFacade.setInEmbeddedMode(true);
        try {
            JavaCheckerFacade.init();
        }catch(ConfigException ex){
            throw new JPEConfigurationException(ex.getMessage(),ex);
        }         
        JavaCheckerFacade.addInputDirectory(configuration_.getInputDir(),true);
        for(String includeDir: configuration_.getIncludeDirs()) {
            JavaCheckerFacade.addInputDirectory(includeDir,false);
        }
        for(String includeJar: configuration_.getIncludeJars()) {
            JavaCheckerFacade.addIncludeJar(includeJar);
        }
        
        
        
        try {
            jpeTransformation_=new JPEPass(configuration_,this);
            modelToJavaSystem_=TermWare.getInstance().getOrCreateDomain("M2J").resolveSystem("M2J");
            modelToJavaSystem_.setFacts(jpeFacts_);
        }catch(TermWareException ex){
            ex.printStackTrace();
            throw new JPEConfigurationException("exception during init",ex);
        }
    }
    
    
    public void run() throws JPEConfigurationException, JPEProcessingException {
        
        if (CompileTimeConstants.DEMO || ! configuration_.isSilent()) {
            if (CompileTimeConstants.DEMO) {
                System.out.println("JPE version "+CompileTimeConstants.VERSION+" (demo)");                
            }else{
                System.out.println("JPE version "+CompileTimeConstants.VERSION);
            }
            System.out.println("(C) Grad-Soft Ltd, 2007-2010");
            System.out.println("http://www.gradsoft.ua");            
        }
        
        boolean twoPasses = configuration_.isUnreachableCodeEliminationEnabled()||configuration_.isDevirtualizationEnabled();
        
        String firstOutputDir;
        if (twoPasses) {
            firstOutputDir = JavaCheckerFacade.getTmpDir()+File.separator+"jpe";
        }else{
            firstOutputDir = configuration_.getOutputDir();
        }
        
        run(1,jpeTransformation_,configuration_.getInputDir(),firstOutputDir);
        
        
        if (twoPasses) {
            
            // now reinit JavaChecker
            jpeFacts_.getUnitsToProcess().clear();
            JavaCheckerFacade.getPackagesStore().clear();
            JavaCheckerFacade.addInputDirectory(configuration_.getInputDir(),true);
            for(String includeDir: configuration_.getIncludeDirs()) {
                JavaCheckerFacade.addInputDirectory(includeDir,false);
            }
            for(String includeJar: configuration_.getIncludeJars()) {
                JavaCheckerFacade.addIncludeJar(includeJar);
            }
            
            run(2,SecondCollectPass.INSTANCE,firstOutputDir,null);
            
            run(3,ReachabilityAnalysisPass.INSTANCE,null,null);
            jpeFacts_.afterReachabilityAnalysis();
            run(4,DevirtualizationAnalysisPass.INSTANCE,null,null);
            jpeFacts_.afterDevirtualizationAnalysis();
            run(5,OutputPass.INSTANCE,null,configuration_.getOutputDir());
            removeDirectory(firstOutputDir);
        }
        
        
    }
    
    
    private void run(int nPass, Pass pass, String inputDir, String outputDir) throws JPEConfigurationException, JPEProcessingException {
        if (configuration_.getDebugLevel() >= DebugLevels.SHOW_PASSES) {
            System.out.println("run nPass="+nPass+", "+pass.getClass().getName());
        }
        if (pass.isReadInput()) {
            collectCompilationUnits(inputDir);
        }
        if (pass.isWalk() || pass.isWriteOutput()) {
            for(AnalyzedUnitRef unitRef: jpeFacts_.getUnitsToProcess()) {
                if (!configuration_.isSilent() && configuration_.getDebugLevel() >= DebugLevels.SHOW_FILES ) {
                    System.out.println("analyzing:"+unitRef.getDirectory()+File.separator+unitRef.getResource());
                }
                JavaUnitModel jm = null;
                try {
                    jm=unitRef.getJavaUnitModel();
                }catch(TermWareException ex){
                    throw new JPEProcessingException("can't get model for "+unitRef.toString()+" in pass "+pass,ex);
                }
                List<Term> results = new LinkedList<Term>();
                List<JavaTypeModel> typeModels = jm.getTypeModels();
                try{
                    if (pass.isWalk()) {
                        for(JavaTypeModel tm: typeModels) {
                            Term resultTerm = pass.transform(tm,jpeFacts_,configuration_);
                            if (pass.isWriteOutput()) {
                                if (!CompileTimeConstants.DEMO && (configuration_.getDebugLevel() >= DebugLevels.SHOW_DUMS ||  configuration_.isDump())) {
                                    System.err.println("after analyzing:");
                                    resultTerm.println(System.err);
                                }
                                results.add(resultTerm);
                            }
                        }
                    }else{
                        if (pass.isWriteOutput()) {
                            for(JavaTypeModel tm: typeModels) {
                                results.add(tm.getModelTerm());
                            }
                        }
                    }
                }catch(InvalidJavaTermException ex){
                    throw new JPEProcessingException("Error during processing "+ex.getFileAndLine().toString(),ex);
                }catch(TermWareException ex){
                    throw new JPEProcessingException("Error during processing ",ex);
                }catch(EntityNotFoundException ex){
                    throw new JPEProcessingException("Error during processing "+ex.getFileAndLine().toString(),ex);
                }
                if (pass.isWriteOutput() && typeModels.size()>0) {
                    try {
                        Term outCuTerm = createOutCompilationUnitTerm(results,typeModels);
                        if (CompileTimeConstants.DEBUG && (configuration_.isDump() || configuration_.getDebugLevel() >= DebugLevels.SHOW_DUMS)) {
                            System.err.println("outCuTerm:");
                            outCuTerm.println(System.err);
                        }
                        printCompilationUnit(outCuTerm, typeModels.get(0).getFullName(), outputDir);
                    }catch(TermWareException ex){
                        throw new JPEProcessingException("Error during processing",ex);
                    }
                }
            }
        }else{
            // nothing.
        }
    }
    
    private void collectCompilationUnits(String inputDir) throws JPEConfigurationException, JPEProcessingException {
        String packageDir="";
        File inputDirFile=new File(inputDir);
        if (inputDirFile.exists()) {
            if (inputDirFile.isDirectory()) {
                /* all is ok */
            }else{
                throw new JPEConfigurationException("input dir "+inputDir+" is not directory");
            }
        }else{
            throw new JPEConfigurationException("input dir "+inputDir+" does not exists");
        }
        collectCompilationUnits(packageDir,inputDir,inputDirFile);
    }
    
    private void collectCompilationUnits(String packageDir,String sourceDir,File d) throws JPEProcessingException{
        File[] files=d.listFiles();
        for(int i=0; i<files.length; ++i) {
            if (files[i].isDirectory()) {
                String nextPackageDir = (packageDir.length()==0 ? files[i].getName() : packageDir+"."+files[i].getName());
                collectCompilationUnits(nextPackageDir, sourceDir, files[i]);
            }else if(files[i].getName().endsWith(".java")) {
                collectFile(packageDir, sourceDir, files[i]);
            }
        }
    }
    
    private void collectFile(String packageDir, String sourceDir, File f) throws JPEProcessingException {
        if (configuration_.getDebugLevel() >= DebugLevels.SHOW_FILES) {
            System.out.println("collect file:"+f.getAbsolutePath());
        }
        Reader reader = null;
        try {
            reader = new FileReader(f);
        }catch(FileNotFoundException ex){
            throw new JPEProcessingException("error during opening file "+f.getAbsolutePath(),ex);
        }
        Term nil=TermWare.getInstance().getTermFactory().createNIL();
        Term source=null;
        try {
            source=TermWare.getInstance().getParserFactory("Java").createParser(reader,f.getAbsolutePath(),nil,TermWare.getInstance()).readTerm();
        }catch(TermWareException ex){
            throw new JPEProcessingException("exception during reading file "+f.getAbsolutePath(),ex);
        }
        
        if (source.getArity()==0) {
            // empty file: possible, skip one.
        }else{
            String packageSrcName;
            try {
                packageSrcName = JUtils.getCompilationUnitPackageName(source);
            }catch(TermWareException ex){
                throw new JPEProcessingException("Can't get package name for "+f.getAbsolutePath(),ex);
            }
            JavaPackageModel pm=JavaCheckerFacade.getPackagesStore().findOrAddPackage(packageSrcName);
            JavaCompilationUnitModel cu = new JavaCompilationUnitModel(f.getAbsolutePath());
            cu.setPackageModel(pm);
            AnalyzedUnitRef ref = new AnalyzedUnitRef(AnalyzedUnitType.SOURCE,sourceDir+"/"+packageDir.replace('.','/'),f.getName(),cu);
            try {
                pm.addCompilationUnit(source,cu,ref);
            }catch(InvalidJavaTermException ex){
                FileAndLine fl = ex.getFileAndLine();
                System.err.print("error during reading sources at " +fl.toString());
                throw new JPEProcessingException("error during reading sources at "+fl.toString(),ex);
            }catch(TermWareException ex){
                System.err.print("error during reading sources");
                throw new JPEProcessingException("error during reading sources",ex);
            }
            
            
            jpeFacts_.getUnitsToProcess().add(ref);
            
            if (!CompileTimeConstants.DEMO && (configuration_.getDebugLevel() >= DebugLevels.SHOW_DUMS ||  configuration_.isDump())) {
                try{
                    for(JavaTypeModel tm: cu.getTypeModels()) {
                        Term modelTerm = tm.getModelTerm();
                        System.err.println("readed model:");
                        modelTerm.println(System.err);
                    }                                        
                }catch(TermWareException ex){
                    System.err.print("error during printing readed sources");
                    throw new JPEProcessingException("error during printing sources",ex);
                }catch(EntityNotFoundException ex){
                    throw new JPEProcessingException("error during printing sources",ex);
                }
            }
            
        }
    }
    
    
    private void processTypeModel(JavaTermTypeAbstractModel tm) throws TermWareException {
        if (configuration_.isDevirtualizationEnabled()) {
            MarkNumberOfChildsInSuper.process(tm);
        }
    }
    
    
    /**
     * create packages and import declarations.
     *@param types -- list of Model terms of types
     */
    private Term createOutCompilationUnitTerm(List<Term> models, List<JavaTypeModel> types) throws TermWareException {
        if (types.size()>0) {
            List<Term> importDeclarations = createImportDeclarations(models);
            List<Term> jtypes = modelToJava(models);
            Term packageTerm = JPEUtils.createPackageTerm(types.get(0));
            ArrayList<Term> cul0 = new ArrayList<Term>();
            cul0.add(packageTerm);
            cul0.addAll(importDeclarations);
            cul0.addAll(jtypes);
            //Term cul = TermWare.getInstance().getTermFactory().createList(cul0);
            Term[] cul = cul0.toArray(new Term[0]);
            Term cu = TermWare.getInstance().getTermFactory().createTerm("CompilationUnit",cul);
            return cu;
        }else{
            return TermWare.getInstance().getTermFactory().createNil();
        }
    }
    
    /**
     *TODO: implement
     */
    private List<Term> createImportDeclarations(List<Term> types) {
        return Collections.emptyList();
    }
    
    /**
     *@param input -- list of model terms (already transformed)
     *@returns -- list of appropriative java terms.
     */
    private List<Term> modelToJava(List<Term> models) throws TermWareException {
        List<Term> rl = new LinkedList<Term>();
        for(Term m: models) {
            Term rj = modelToJavaSystem_.reduce(m);
            rl.add(rj);
        }
        return rl;
    }
    
    private void printCompilationUnit(Term cu, String typeName, String outputDir) throws JPEProcessingException {
        String sTransformed = null;
        try {
            sTransformed=TermHelper.termToPrettyString(cu,"Java",TermWare.getInstance().getTermFactory().createNIL());
        }catch(TermWareException ex){
            cu.println(System.err);
            throw new JPEProcessingException("exception during printing resuts for "+ typeName,ex);
        }
        String packageName = "";
        try {
            packageName=JUtils.getCompilationUnitPackageName(cu);
        }catch(TermWareException ex){
            ex.printStackTrace();
            throw new JPEProcessingException("exception during getting package name for results for "+typeName,ex);
        }
        String firstClassName = null;
        try {
            firstClassName = JUtils.getFirstTypeDefinitionName(cu);
        }catch(TermWareException ex){
            throw new JPEProcessingException("exception during getting type declaration name for results for "+typeName,ex);
        }catch(EntityNotFoundException ex){
            // file without type declaration, i. e. empty.
            // just do nothing.
            if (false) {
                ex.printStackTrace();
                throw new JPEProcessingException("exception during getting type declaration name for results for "+typeName,ex);
            }else{
                return;
            }
        }
        FileWriter writer = null;
        String destinationPackageDir=JUtils.createDirectoryNameFromPackageName(outputDir,packageName);
        File destinationDirFile = new File(destinationPackageDir);
        if (!destinationDirFile.exists()) {
            destinationDirFile.mkdirs();
        }
        String outputFname=destinationPackageDir+File.separator+JUtils.createSourceFileNameFromClassName(firstClassName);
        try {
            writer=new FileWriter(outputFname);
            writer.append(sTransformed);
        }catch(IOException ex){
            throw new JPEProcessingException("exception during processing "+typeName,ex);
        }finally{
            if (writer!=null){
                try {
                    writer.close();
                }catch(IOException ex){
                    System.err.println("exception during closing file "+outputFname);
                }
            }
        }
        
        
    }
    
    private void removeDirectory(String dirname) {
        removeFileOrDirectory(new File(dirname));
    }
    
    private void removeFileOrDirectory(File f)
    {
      if (f.isDirectory())  {
         File[] childs = f.listFiles();
         for(int i=0; i<childs.length; ++i){
             removeFileOrDirectory(childs[i]);
         }
      }
      f.delete();
    }
    
    void setJPEFacts(JPEFacts facts) {
        jpeFacts_=facts;
    }
    
    private TermSystem     getModelToJavaSystem() {
        return modelToJavaSystem_;
    }
    
    
    private JPEPass jpeTransformation_=null;
    private TermSystem    modelToJavaSystem_=null;
    private Configuration configuration_=new Configuration();
    private JPEFacts      jpeFacts_;
    
    
}
