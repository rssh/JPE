/*
 * MarkNumberOfChildsInSuper.java
 *
 * Created on October 2, 2007, 6:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ua.gradsoft.jpe.optimizations;

import ua.gradsoft.javachecker.EntityNotFoundException;
import ua.gradsoft.javachecker.JavaFacts;
import ua.gradsoft.javachecker.checkers.JavaTypeModelOnePassProcessor;
import ua.gradsoft.javachecker.models.JavaTermTypeAbstractModel;
import ua.gradsoft.javachecker.models.JavaTypeModel;
import ua.gradsoft.javachecker.models.TermUtils;
import ua.gradsoft.termware.Term;
import ua.gradsoft.termware.TermWareException;
import ua.gradsoft.termware.exceptions.AssertException;

/**
 *
 * @author rssh
 */
public class MarkNumberOfChildsInSuper 
{
      
    public static final String ATTRIBUTE_NAME = "N_DERIVED";

    
    public static void process(JavaTermTypeAbstractModel tm) throws TermWareException
    {
      if (tm.isClass()) {         
          JavaTypeModel superClass = null;
          try {
             superClass = tm.getSuperClass();
          }catch(EntityNotFoundException ex){
              throw new AssertException(ex.getMessage()+" at "+ex.getFileAndLine().toString(),ex);
          }
          if (superClass!=null) {
              Term nDerivedTerm = superClass.getAttribute(ATTRIBUTE_NAME);
              if (nDerivedTerm.isNil()) {
                  superClass.setAttribute(ATTRIBUTE_NAME,TermUtils.createInt(1));
              }else{
                  int nDerived = nDerivedTerm.getInt();
                  superClass.setAttribute(ATTRIBUTE_NAME,TermUtils.createInt(nDerived+1));                  
              }
          }
      }
    }
    
    
  
    
}
