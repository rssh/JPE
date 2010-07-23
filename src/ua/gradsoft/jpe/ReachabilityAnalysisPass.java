/*
 * ReachabilityAnalysisPass.java
 *
 * Created on October 5, 2007, 1:28 AM
 *
 */

package ua.gradsoft.jpe;

import ua.gradsoft.javachecker.EntityNotFoundException;
import ua.gradsoft.javachecker.models.JavaTypeModel;
import ua.gradsoft.jpe.optimizations.MarkReachable;
import ua.gradsoft.termware.Term;
import ua.gradsoft.termware.TermWareException;

/**
 *
 * @author rssh
 */
public class ReachabilityAnalysisPass implements Pass
{
    
    public boolean isReadInput() {
        return false;
    }        

    public boolean isWriteOutput() {
        return false;
    }
    
    public boolean isWalk()
    {
        return true;
    }
        
    
    public Term  transform(JavaTypeModel tm, JPEFacts facts, Configuration configuration) throws TermWareException, EntityNotFoundException
    {
             if (isReachablePoint(tm,configuration)) {
                 MarkReachable.markExternal(tm,configuration);
             }
             return null;
    }
    
    private boolean isReachablePoint(JavaTypeModel tm,Configuration configuration)
    {
        String fullname = tm.getFullName();
        for(String s: configuration.getReachableClassPatterns()) {
            if (fullname.matches(s)) {
                return true;
            }
        }
        return false;
    }
    
    public static ReachabilityAnalysisPass INSTANCE = new ReachabilityAnalysisPass();
}
