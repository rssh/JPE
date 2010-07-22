/*
 * ParsePrintParseTest.java
 *
 *
 * Copyright (c) 2006, 2007 GradSoft  Ukraine
 * http://www.gradsoft.ua
 * All Rights Reserved
 */


package ua.gradsoft.jpe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import junit.framework.TestCase;
import ua.gradsoft.termware.IParser;
import ua.gradsoft.termware.IParserFactory;
import ua.gradsoft.termware.Term;
import ua.gradsoft.termware.TermSystem;
import ua.gradsoft.termware.TermWare;
import ua.gradsoft.termware.exceptions.AssertException;

/**
 *
 * @author Ruslan Shevchenko
 */
public class ParsePrintParseTest extends TestCase
{
    
    public void testExample1() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.setJPEHome(".");
        configuration.setInputDir("testdata/example1");
        configuration.setOutputDir("output/id-example1");
        configuration.setTransformationName("ID");
        configuration.setCreateOutputDir(true);
        Main main = new Main();
        main.init(configuration);
        main.run();
        int nReadedFiles = checkAllSourcesAreParsed("output/id-example1");
        assertTrue("all files are read",nReadedFiles>0);
    }
    
    public void testExample2() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.setJPEHome(".");
        configuration.setInputDir("testdata/example2");
        configuration.setOutputDir("output/id-example2");
        configuration.setTransformationName("ID");
        configuration.setCreateOutputDir(true);
        Main main = new Main();
        main.init(configuration);
        main.run();
        int nReadedFiles = checkAllSourcesAreParsed("output/id-example2");
        assertTrue("all files are read",nReadedFiles>0);
    }
    
    public void testExample3() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.setJPEHome(".");
        configuration.setInputDir("testdata/example3");
        configuration.setOutputDir("output/id-example3");
        configuration.setTransformationName("ID");
        configuration.setCreateOutputDir(true);
        Main main = new Main();
        main.init(configuration);
        main.run();
        int nReadedFiles = checkAllSourcesAreParsed("output/id-example3");
        assertTrue("all files are read",nReadedFiles>0);
    }
    
    public void testExample4() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.setJPEHome(".");
        configuration.setInputDir("testdata/example4");
        configuration.setOutputDir("output/id-example4");
        configuration.setTransformationName("ID");
        configuration.setCreateOutputDir(true);
        Main main = new Main();
        main.init(configuration);
        main.run();
        int nReadedFiles = checkAllSourcesAreParsed("output/id-example4");
        assertTrue("all files are read",nReadedFiles>0);        
    }

    public void testExample5() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.setJPEHome(".");
        configuration.setInputDir("testdata/example5");
        configuration.setOutputDir("output/id-example5");
        configuration.setTransformationName("ID");
        configuration.setCreateOutputDir(true);
        Main main = new Main();
        main.init(configuration);
        main.run();
        int nReadedFiles = checkAllSourcesAreParsed("output/id-example5");
        assertTrue("all files are read",nReadedFiles>0);        
    }
    
    public void testExample1_JPE1_t() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.setJPEHome(".");
        configuration.setInputDir("testdata/example1");
        configuration.setOutputDir("output/jpe-example1-t");
        configuration.getCompileTimeProperties().put("DEBUG","true");
        configuration.setTransformationName("JPE");
        configuration.setCreateOutputDir(true);
        Main main = new Main();
        main.init(configuration);
        main.run();
        int nReadedFiles = checkAllSourcesAreParsed("output/jpe-example1-t");
        assertTrue("all files are read",nReadedFiles>0);        
    }
    
    public void testExample1_JPE1_f() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.setJPEHome(".");
        configuration.setInputDir("testdata/example1");
        configuration.setOutputDir("output/jpe-example1-f");
        configuration.getCompileTimeProperties().put("DEBUG","false");
        configuration.setTransformationName("JPE");
        configuration.setCreateOutputDir(true);
        Main main = new Main();
        main.init(configuration);
        main.run();
        int nReadedFiles = checkAllSourcesAreParsed("output/jpe-example1-f");
        assertTrue("all files are read",nReadedFiles>0);        
    }
    
    public void testExample6_JPE1_1() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.setJPEHome(".");
        configuration.setInputDir("testdata/example6");
        configuration.setOutputDir("output/jpe-example6-1");
        configuration.getCompileTimeProperties().put("q1","1");
        configuration.setTransformationName("JPE");
        configuration.setCreateOutputDir(true);
        Main main = new Main();
        main.init(configuration);
        main.run();
        int nReadedFiles = checkAllSourcesAreParsed("output/jpe-example6-1");
        assertTrue("all files are read",nReadedFiles>0);        
    }
    
    public void testExample7_JPE1_1() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.setJPEHome(".");
        configuration.setInputDir("testdata/example7");
        configuration.setOutputDir("output/jpe-example7-1");
        configuration.getCompileTimeProperties().put("q1","1");
        configuration.setTransformationName("JPE");
        configuration.setCreateOutputDir(true);
        Main main = new Main();
        main.init(configuration);
        TermSystem ts = TermWare.getInstance().getDomain("M2J").resolveSystem("M2J");
        //ts.setLoggingMode(true);
        //ts.setLoggedEntity("All");
        main.run();
        int nReadedFiles = checkAllSourcesAreParsed("output/jpe-example7-1");
        assertTrue("all files are read",nReadedFiles>0);        
    }

    public void testExample8_JPE() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.setJPEHome(".");
        configuration.setInputDir("testdata/example8");
        configuration.setOutputDir("output/jpe-example8");
        configuration.getCompileTimeProperties().put("q1","1");
        configuration.setTransformationName("JPE");
        configuration.setCreateOutputDir(true);
        Main main = new Main();
        main.init(configuration);
        //TermSystem ts = TermWare.getInstance().getDomain("M2J").resolveSystem("M2J");
        //ts.setLoggingMode(true);
        //ts.setLoggedEntity("All");
        main.run();
        int nReadedFiles = checkAllSourcesAreParsed("output/jpe-example8");
        assertTrue("all files are read",nReadedFiles>0);        
    }
    
    
    
    private int checkAllSourcesAreParsed(String fname) throws Exception
    {
        File f = new File(fname);
        if (f.isDirectory()) {
            return checkSourcesAreParsedInDir(f);
        }else{
            if (f.getName().endsWith(".java")) {
                Term t = parseSource(f);
                return 1;
            }
        }
        return 0;
    }
    
    private int checkSourcesAreParsedInDir(File f) throws Exception
    {        
        if (f.isDirectory()) {
            int nProcessed=0;
            File[] sfs = f.listFiles();   
            for(File sf: sfs) {
                System.err.print(":"+sf.getName()+":");
            }
            for(File sf: sfs) {                
                if (sf.isDirectory()) {                    
                    nProcessed+=checkSourcesAreParsedInDir(sf);
                }else if(sf.getCanonicalPath().endsWith(".java")) {                    
                    Term t = parseSource(sf);
                    ++nProcessed;
                }else{                    
                    throw new AssertException("Non-Java file in output:"+f.getCanonicalPath());
                }
            }
            return nProcessed;
        }else{
            throw new RuntimeException("argument of checkSourcesAreParsedInDir must be directory");
        }        
    }
    
    private Term parseSource(File f) throws Exception
    {
        IParserFactory parserFactory = TermWare.getInstance().getParserFactory("Java");
        Reader reader=null;
        Term retval=null;
        try {
            reader=new BufferedReader(new FileReader(f));
            IParser parser=parserFactory.createParser(reader,f.getAbsolutePath(),TermWare.getInstance().getTermFactory().createNIL(),TermWare.getInstance());
            retval=parser.readTerm();
        }finally{
            if(reader!=null){
                reader.close();
            }
        }
        return retval;
    }
    
}
