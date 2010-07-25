/*
 * MarkNumberOfChildsInSuper.java
 *
 */

package ua.gradsoft.jpe.optimizations;

import java.util.Map;
import ua.gradsoft.javachecker.EntityNotFoundException;
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
      Map<String,JavaTypeModel> nested = tm.getNestedTypeModels();
      for(JavaTypeModel t: nested.values()) {
          process((JavaTermTypeAbstractModel)t);
      }
    }
    
    
  
    
}
