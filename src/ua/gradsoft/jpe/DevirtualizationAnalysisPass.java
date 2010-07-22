/*
 * DevirtualizationAnalysisPass.java
 *
 * Created on October 5, 2007, 4:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ua.gradsoft.jpe;

import ua.gradsoft.javachecker.EntityNotFoundException;
import ua.gradsoft.javachecker.models.JavaTermTypeAbstractModel;
import ua.gradsoft.javachecker.models.JavaTypeModel;
import ua.gradsoft.jpe.optimizations.MarkNumberOfChildsInSuper;
import ua.gradsoft.jpe.optimizations.MarkReachable;
import ua.gradsoft.termware.Term;
import ua.gradsoft.termware.TermWareException;

/**
 *Call devirtualization analysis.
 * @author rssh
 */
public class DevirtualizationAnalysisPass implements Pass
{
    
    
    public boolean isReadInput() {
        return false;
    }
    
    
    public boolean isWalk() {
        return true;
    }
       

    public boolean isWriteOutput() {
        return false;
    }
    
    
    public Term transform(JavaTypeModel typeModel, JPEFacts facts, Configuration configuration) throws TermWareException, EntityNotFoundException {
        if (configuration.isUnreachableCodeEliminationEnabled()) {            
           // check only if current class is reachable.
           Term reachable = typeModel.getAttribute(MarkReachable.REACHABLE_CLASS_ATTRIBUTE);
           if (!reachable.isNil()) {
             if (typeModel instanceof JavaTermTypeAbstractModel) {  
                MarkNumberOfChildsInSuper.process((JavaTermTypeAbstractModel)typeModel);
             }
           }else{
             if (configuration.getDebugLevel() >= DebugLevels.SHOW_WALK)  {
                 System.out.println("devirtualization skip "+typeModel.getFullName()+" as unreachable");
             }
           }
        }else{
           MarkNumberOfChildsInSuper.process((JavaTermTypeAbstractModel)typeModel); 
        }
        return null;
    }
    
    public static final DevirtualizationAnalysisPass INSTANCE = new DevirtualizationAnalysisPass();
    
}
