/*
 * JPEPass.java
 *
 */

package ua.gradsoft.jpe;

import ua.gradsoft.javachecker.EntityNotFoundException;
import ua.gradsoft.javachecker.models.JavaTypeModel;
import ua.gradsoft.termware.IFacts;
import ua.gradsoft.termware.Term;
import ua.gradsoft.termware.TermSystem;
import ua.gradsoft.termware.TermWare;
import ua.gradsoft.termware.TermWareException;
import ua.gradsoft.termware.exceptions.AssertException;

/**
 *Transformation, which do partial ealuation.
 * @author rssh
 */
public class JPEPass implements Pass
{
    
    /**
     * Creates a new instance of JPEPass
     */
    public JPEPass(Configuration configuration, Main main) throws TermWareException
    {
            jpeSystem_=TermWare.getInstance().getOrCreateDomain("JPE").resolveSystem(configuration.getTransformationName());
            idSystem_=TermWare.getInstance().getOrCreateDomain("JPE").resolveSystem("ID");        
            IFacts facts = jpeSystem_.getFacts();
            if (facts instanceof JPEFacts) {
                ((JPEFacts)facts).setConfiguration(configuration);
                main.setJPEFacts((JPEFacts)facts);
            }else{
                throw new AssertException("JPE resolver must be JPEFacts");
            }
    }
    
    public boolean isReadInput()
    { return true; }
    
    public boolean isWriteOutput()
    { return true; }
    
    public boolean isWalk()
    { return true; }
    
    public Term  transform(JavaTypeModel tm, JPEFacts facts, Configuration configuration) throws TermWareException, EntityNotFoundException
    {
               String transformation = configuration.getTransformationName();
               Term modelTerm = tm.getModelTerm();
               TermSystem ts = jpeSystem_;
               if (configuration.isDisabledClass(tm.getErasedFullName())) {
                   transformation = "ID";
                   ts = idSystem_;
               }
               Term argTerm = TermWare.getInstance().getTermFactory().createTerm(transformation,modelTerm);
               if (CompileTimeConstants.DEBUG && configuration.getDebugLevel() >= DebugLevels.SHOW_WALK) {
                      System.err.println("start "+transformation+" reduce for "+tm.getFullName()); 
               }
               Term resultTerm = ts.reduce(argTerm);
               return resultTerm;
    }
    
    
    private TermSystem    jpeSystem_=null;
    private TermSystem    idSystem_=null;
 
    
}
