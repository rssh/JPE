/*
 * JPEFacts.java
 *
 *
 * Copyright (c) 2007 GradSoft  Ukraine
 * All Rights Reserved
 */


package ua.gradsoft.jpe;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import ua.gradsoft.javachecker.EntityNotFoundException;
import ua.gradsoft.javachecker.models.AnalyzedUnitRef;
import ua.gradsoft.javachecker.models.JavaExpressionModel;
import ua.gradsoft.javachecker.models.JavaMemberVariableModel;
import ua.gradsoft.javachecker.models.JavaMethodModel;
import ua.gradsoft.javachecker.models.JavaModifiersModel;
import ua.gradsoft.javachecker.models.JavaTopLevelBlockOwnerModel;
import ua.gradsoft.javachecker.models.JavaTypeModel;
import ua.gradsoft.javachecker.models.JavaVariableModel;
import ua.gradsoft.javachecker.models.TermUtils;
import ua.gradsoft.jpe.optimizations.MarkNumberOfChildsInSuper;
import ua.gradsoft.jpe.optimizations.MarkReachable;
import ua.gradsoft.termware.DefaultFacts;
import ua.gradsoft.termware.IParser;
import ua.gradsoft.termware.IParserFactory;
import ua.gradsoft.termware.Term;
import ua.gradsoft.termware.TermWare;
import ua.gradsoft.termware.TermWareException;
import ua.gradsoft.termware.TransformationContext;
import ua.gradsoft.termware.exceptions.AssertException;

/**
 *Facts for JPE
 * @author Ruslan Shevchenko
 */
public class JPEFacts extends DefaultFacts
{
    
    public JPEFacts() throws TermWareException
    { super(); }
    
    public void setConfiguration(Configuration configuration)
    {
      configuration_=configuration;  
      unitsToProcess_ = new LinkedList<AnalyzedUnitRef>();
      isAfterReachabilityAnalysis_=false;
      isAfterDevirtualizationAnalysis_=false;      
    }    
    
    //
    // Facts methods
    //
    
    /**
     *resolve name, if it is one which needed 
     */
    public boolean isJPEField(TransformationContext ctx,JavaVariableModel v,Term to) throws TermWareException
    {
       if (!to.isX()) {
          throw new AssertException("second term argument of resolveName must be a propositional variable");
      }
      
      boolean retval=false;
      
      switch(v.getKind()){
          case FORMAL_PARAMETER:                                 
          case LOCAL_VARIABLE:
              break;
          case MEMBER_VARIABLE:
          {
              JavaMemberVariableModel mv = (JavaMemberVariableModel)v;
              JavaTypeModel tm = mv.getOwnerType();
              String tmName = tm.getFullName();
              if (tmName.endsWith(configuration_.getCompileTimeClassname())) {                
                  String value = configuration_.getCompileTimeProperties().get(mv.getName());
                  //System.err.print("getCompileTimeProperty for "+mv.getName());
                  //if (value==null) {
                  //    System.err.println("null");
                  //}else{
                  //    System.err.println(value);
                  //}
                  if (value!=null) {      
                     StringReader reader = new StringReader(value);
                     Term optionTerm = TermWare.getInstance().getTermFactory().createAtom("Expression");
                     IParserFactory parserFactory = TermWare.getInstance().getParserFactory("Java");
                     IParser parser = parserFactory.createParser(reader,"inline",optionTerm,TermWare.getInstance());
                     Term t = parser.readTerm();
                     ctx.getCurrentSubstitution().put(to,t);
                     retval=true;
                  }                                        
              }
          }
          break;
          default:
              throw new AssertException("Invalid variable kind:"+v.getKind());
              
      }
      
      return retval;
    }
    
    public boolean isVarArgsIntModifier(int x)
    {
        return (JavaModifiersModel.VARARGS & x)!=0 ;
    }
   
    /**
     *resolve name, if it is one which needed 
     */
    public boolean isStaticFinalLiteralField(TransformationContext ctx,JavaVariableModel v,Term to) throws TermWareException
    {
       if (!to.isX()) {
          throw new AssertException("second term argument of isStaticFinalLiteralField must be a propositional variable");
      }
      
      boolean retval=false;
      
      switch(v.getKind()){
          case FORMAL_PARAMETER:                                 
          case LOCAL_VARIABLE:
              break;
          case MEMBER_VARIABLE:
          {
            try {  
              JavaMemberVariableModel mv = (JavaMemberVariableModel)v;
              JavaModifiersModel modifiers = mv.getModifiers();
              if (modifiers.isStatic() && modifiers.isFinal()) {
                  JavaExpressionModel e = mv.getInitializerExpression();
                  if (e!=null) {                      
                      boolean isLiteral=false;                      
                      switch(e.getKind()) {
                          case BOOLEAN_LITERAL:
                          case CHARACTER_LITERAL:
                          case CLASS_LITERAL:
                          case FLOATING_POINT_LITERAL:
                          case INTEGER_LITERAL:
                              isLiteral=true;
                              break;
                          default:
                              break;
                      }
                      if (isLiteral) {
                        Term me = e.getModelTerm();
                        ctx.getCurrentSubstitution().put(to,me);
                        retval=true;
                      }
                  }
              }
            }catch(EntityNotFoundException ex){
                throw new AssertException(ex.getMessage(),ex);
            }
          }
          break;
          default:
              throw new AssertException("Invalid variable kind:"+v.getKind());
              
      }
      
      return retval;
        
    }
       
    /**
     * true if
     */
    public boolean isCanBeFinalForDevirtualization(JavaTypeModel tm) throws TermWareException
    {
      //  if (configuration_==null) {
      //      System.out.println("configration_==null");
      //  }
        if (configuration_.isDevirtualizationEnabled() && isAfterDevirtualizationAnalysis() ) {     
         if (configuration_.getDebugLevel() >= DebugLevels.SHOW_WALK)  {
             System.out.println("check for possible final: "+tm.getFullName());
         }                                    
         if (tm.isClass()) {
          if (tm.getModifiersModel().isAbstract()) {              
              return false;
          }          
          Term x = tm.getAttribute(MarkNumberOfChildsInSuper.ATTRIBUTE_NAME);
          if (x.isNil()) {
            String tmname=tm.getFullName();  
            for(String classnamepattern: configuration_.getNonFinalClassPatterns()) {
                if (tmname.matches(classnamepattern)) {
                    return false;
                }
            }    
            return true;
          }else{
              if (configuration_.getDebugLevel()>= DebugLevels.SHOW_WALK) {
                  System.out.println("Number of childs:"+x.getInt());
              }
          }
         }
        }else{
    //      if (CompileTimeConstants.DEBUG) {  
    //        System.out.println("isCanBeFinalForDevirtualization("+tm.getFullName()+") is not enabled yet");
    //      }
        }
        return false;
    }
    
    public boolean isNotReachable(JavaTypeModel typeModel) throws TermWareException
    {
      if (configuration_.isUnreachableCodeEliminationEnabled() && isAfterReachabilityAnalysis())  {
          Term rt = typeModel.getAttribute(MarkReachable.REACHABLE_CLASS_ATTRIBUTE);
          if (rt.isNil()) {
              if (CompileTimeConstants.DEMO || !configuration_.isSilent()) {                  
                System.out.println("unreachable class will be eliminated:"+typeModel.getFullName());  
              }
              return true;
          }else{
              return false;
          }
      }else{
          return false;
      }
    }
    
    public boolean isNotReachableField(JavaMemberVariableModel v) throws TermWareException
    {        
        if (configuration_.isUnreachableCodeEliminationEnabled() && isAfterReachabilityAnalysis()) {
            // for now = check that field is private, later: check that complete unreachable.
            if (!v.getModifiers().isPrivate()) {
                return false;
            }
            Term rt = v.getAttribute(MarkReachable.REACHABLE_FIELD);
            if (rt.isNil()) {              
                JavaTypeModel mto = v.getOwnerType();
                Term mtort = mto.getAttribute(MarkReachable.REACHABLE_CLASS_ATTRIBUTE);
                if (mtort.isNil()) {
                    // warning -- this must not be asked in reachable field.
                 //  System.out.println("warning: we ask for reachability of field "+v.getName()+" in non-reachable class "+mto.getFullName());                      
                   return false;
                }else{
                  if (CompileTimeConstants.DEMO || !configuration_.isSilent()) {
                      System.out.println("unreachable field "+v.getName()+" will be eliminated in "+v.getOwnerType().getFullName());
                  }
                  return true;
                }
            }else{
                return false;
            }
        }else{
            return false;
        }                
    }
    
    public boolean isNotReachableMethod(JavaTopLevelBlockOwnerModel bo) throws TermWareException
    {
        if (configuration_.isUnreachableCodeEliminationEnabled() && isAfterReachabilityAnalysis()) {
            if (bo instanceof JavaMethodModel ) {
                JavaMethodModel mbo = (JavaMethodModel)bo;
                if (!mbo.getModifiers().isPrivate()) {
                    // TODO: handle non-privqte unreachable.
                    return false;
                }
            }else{
                // TODO: check constructors and initializers
                return false;
            }
            Term rt = bo.getAttribute(MarkReachable.REACHABLE_METHOD);
            if (rt.isNil()) {
                if (CompileTimeConstants.DEMO || !configuration_.isSilent()) {
                   System.out.println("unreachable method "+bo.getName()+" will be eliminated in "+bo.getTypeModel().getFullName());
                }
                return true;
                //return false;
            }else{
                return false;
            }
        }else{
            return false;
        }     
    }
    
   
    
    public void addFinalBitfield(TransformationContext ctx, int x,Term to) throws TermWareException
    {
       if (!to.isX()) {
          throw new AssertException("second term argument of addFinalbitfield must be a propositional variable");
      }

      int y = (x | JavaModifiersModel.FINAL);
      ctx.getCurrentSubstitution().put(to,TermUtils.createInt(y));
       
    }
    
    public List<AnalyzedUnitRef> getUnitsToProcess()
    { return unitsToProcess_; }
    
    public boolean isAfterReachabilityAnalysis()
    { return isAfterReachabilityAnalysis_; }
    
    public void afterReachabilityAnalysis()
    { isAfterReachabilityAnalysis_=true; }
    
    public boolean isAfterDevirtualizationAnalysis()
    { return isAfterDevirtualizationAnalysis_; }
    
    public void afterDevirtualizationAnalysis()
    { isAfterDevirtualizationAnalysis_=true; }
    
    
    private Configuration configuration_;
    
    private List<AnalyzedUnitRef>  unitsToProcess_;
    
    private boolean  isAfterReachabilityAnalysis_=false;
    private boolean  isAfterDevirtualizationAnalysis_=false;
    
}
