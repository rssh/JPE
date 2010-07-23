/*
 * MarkReachable.java
 *
 */

package ua.gradsoft.jpe.optimizations;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import ua.gradsoft.javachecker.EntityNotFoundException;
import ua.gradsoft.javachecker.JavaFacts;
import ua.gradsoft.javachecker.NotSupportedException;
import ua.gradsoft.javachecker.models.JavaAnnotationInstanceModel;
import ua.gradsoft.javachecker.models.JavaConstructorModel;
import ua.gradsoft.javachecker.models.JavaExpressionModel;
import ua.gradsoft.javachecker.models.JavaFormalParameterModel;
import ua.gradsoft.javachecker.models.JavaInitializerModel;
import ua.gradsoft.javachecker.models.JavaLocalVariableModel;
import ua.gradsoft.javachecker.models.JavaMemberVariableModel;
import ua.gradsoft.javachecker.models.JavaMethodModel;
import ua.gradsoft.javachecker.models.JavaModifiersModel;
import ua.gradsoft.javachecker.models.JavaResolver;
import ua.gradsoft.javachecker.models.JavaStatementModel;
import ua.gradsoft.javachecker.models.JavaTermMemberVariableModel;
import ua.gradsoft.javachecker.models.JavaTermMethodModel;
import ua.gradsoft.javachecker.models.JavaTermTypeAbstractModel;
import ua.gradsoft.javachecker.models.JavaTopLevelBlockModel;
import ua.gradsoft.javachecker.models.JavaTypeArgumentBoundTypeModel;
import ua.gradsoft.javachecker.models.JavaTypeModel;
import ua.gradsoft.javachecker.models.JavaTypeModelHelper;
import ua.gradsoft.javachecker.models.JavaVariableModel;
import ua.gradsoft.javachecker.models.TermUtils;
import ua.gradsoft.javachecker.models.expressions.JavaTermAllocationExpressionModel;
import ua.gradsoft.javachecker.models.expressions.JavaTermCastExpressionModel;
import ua.gradsoft.javachecker.models.expressions.JavaTermClassLiteralExpressionModel;
import ua.gradsoft.javachecker.models.expressions.JavaTermFieldExpressionModel;
import ua.gradsoft.javachecker.models.expressions.JavaTermFunctionCallExpressionModel;
import ua.gradsoft.javachecker.models.expressions.JavaTermIdentifierExpressionModel;
import ua.gradsoft.javachecker.models.expressions.JavaTermInnerAllocationExpressionModel;
import ua.gradsoft.javachecker.models.expressions.JavaTermInstanceOfExpressionModel;
import ua.gradsoft.javachecker.models.expressions.JavaTermMethodCallExpressionModel;
import ua.gradsoft.javachecker.models.expressions.JavaTermSpecializedMethodCallExpressionModel;
import ua.gradsoft.javachecker.models.expressions.JavaTermStaticFieldExpressionModel;
import ua.gradsoft.jpe.CompileTimeConstants;
import ua.gradsoft.jpe.Configuration;
import ua.gradsoft.jpe.DebugLevels;
import ua.gradsoft.termware.Term;
import ua.gradsoft.termware.TermHelper;
import ua.gradsoft.termware.TermWareException;
import ua.gradsoft.termware.exceptions.AssertException;

/**
 *
 * @author rssh
 */
public class MarkReachable {
    
   public static final  String REACHABLE_CLASS_ATTRIBUTE = "ReachableClass";
   public static final  String REACHABLE_METHOD = "ReachableMethod";
   public static final  String REACHABLE_FIELD = "ReachableField";

 
   
   public static void markExternal(JavaTypeModel typeModel, Configuration configuration) throws TermWareException, EntityNotFoundException
   {              
      if (typeModel.isPrimitiveType()) return; 
      Term rt = typeModel.getAttribute(REACHABLE_CLASS_ATTRIBUTE);
      if (rt.isNil()) {                    
          typeModel.setAttribute(REACHABLE_CLASS_ATTRIBUTE,TermUtils.createBoolean(true));
          if (configuration.getDebugLevel() >= DebugLevels.SHOW_WALK) {
              System.out.println("mark reachable "+typeModel.getFullName());
          }
          if (typeModel instanceof JavaTermTypeAbstractModel) {
              markDepended((JavaTermTypeAbstractModel)typeModel,configuration);
          }else if(typeModel.isArray()){
                markExternal(typeModel.getReferencedType(),configuration);
          }else if(typeModel instanceof JavaTypeArgumentBoundTypeModel){
              JavaTypeArgumentBoundTypeModel ttm = (JavaTypeArgumentBoundTypeModel)typeModel;
              markExternal(ttm.getOrigin(),configuration);
              for(JavaTypeModel tm: ttm.getResolvedTypeArguments()) {
                  markExternal(tm,configuration);
              }
          }          
      }else{
          // already reachable, return.
      }
   }
    
    
   private static void markDepended(JavaTermTypeAbstractModel typeModel,Configuration configuration) throws TermWareException, EntityNotFoundException
   {
     //DEBUG!!!
     boolean debug=false;
    // if (typeModel.getFullName().startsWith("ua.gradsoft.termware.debug.DebugStubGenerator")) {
    //     debug=true;
    // }         
     List<JavaTypeModel> implementsList = typeModel.getSuperInterfaces();
     if (debug) {
       System.out.println("implements lists, size= "+implementsList.size());   
     }
     
     for(JavaTypeModel tm: implementsList) {
         markExternal(tm,configuration);
     }
     JavaTypeModel superClass = typeModel.getSuperClass();
     if (superClass!=null) {
         if (debug) {
           System.out.println("super");
         }
         markExternal(superClass,configuration);
     }
     JavaTypeModel enclosedType = typeModel.getEnclosedType();
     if (enclosedType!=null) {
         if (debug) {
           System.out.println("enclosedType");
         }         
         markExternal(enclosedType,configuration);
     }
     if (debug) {
         System.out.println("annotations.sixe="+typeModel.getAnnotationsMap().entrySet().size());
     }
     for(Map.Entry<String,JavaAnnotationInstanceModel> e: typeModel.getAnnotationsMap().entrySet()) {
         markExternal(e.getValue().getAnnotationModel(),configuration);
     }
     List<JavaInitializerModel> initializers = typeModel.getInitializerModels();
     if (debug) {
         System.out.println("Initializers, size()="+initializers.size());
     }
     for(JavaInitializerModel inm: initializers) {
         if (inm.isSupportBlockModel()) {
               JavaTopLevelBlockModel block = inm.getTopLevelBlockModel();
               markUsage(block, configuration,debug);
         }
     }     
     List<JavaConstructorModel> constructors = typeModel.getConstructorModels();     
     if (debug) {
         System.out.println("Constructors, size()="+constructors.size());
     }     
     for(JavaConstructorModel cn: constructors) {
         for(JavaAnnotationInstanceModel ann: cn.getAnnotationsMap().values()) {
             markExternal(ann.getAnnotationModel(),configuration);
         }
         List<JavaFormalParameterModel> fpms = cn.getFormalParametersList();
         for(JavaFormalParameterModel fpm: fpms) {
             for(JavaAnnotationInstanceModel ann: fpm.getAnnotationsMap().values()) {
                 markExternal(ann.getAnnotationModel(),configuration);
             }
             markExternal(fpm.getType(),configuration);
         }
         List<JavaTypeModel> throwsList = cn.getThrowsList();
         for(JavaTypeModel tm: throwsList) {
             markExternal(tm,configuration);
         }
         if (cn.isSupportBlockModel()) {
               JavaTopLevelBlockModel block = cn.getTopLevelBlockModel();
               markUsage(block, configuration,debug);
         }
     }
     Collection<List<JavaMethodModel>> mss = typeModel.getMethodModels().values();
     if (debug) {
         System.out.println("Methods, size()="+mss.size());
     }     
     
     for(List<JavaMethodModel> ms: mss) {
         for(JavaMethodModel m: ms) {
             for(JavaAnnotationInstanceModel ann: m.getAnnotationsMap().values()) {
                 markExternal(ann.getAnnotationModel(),configuration);
             }
             for(JavaFormalParameterModel fp: m.getFormalParametersList()) {
                 for(JavaAnnotationInstanceModel ann:fp.getAnnotationsMap().values()) {
                     markExternal(ann.getAnnotationModel(),configuration);
                 }
                 markExternal(fp.getType(),configuration);
             }
             for(JavaTypeModel tm: m.getThrowsList()) {
                 markExternal(tm,configuration);
             }
             markExternal(m.getResultType(),configuration);
             if (m.isSupportBlockModel()) {
                    JavaTopLevelBlockModel block = m.getTopLevelBlockModel();
                    if (block!=null) {
                      markUsage(block, configuration, debug);
                    }
             }
         }
     }
     if (debug) {
         System.out.println("Member variables, size()="+typeModel.getMemberVariableModels().values().size());
     }          
     for(JavaMemberVariableModel mv: typeModel.getMemberVariableModels().values()) {
         if (CompileTimeConstants.DEBUG && debug) {
             System.out.println("mv.getName="+mv.getName());
         }
         markExternal(mv.getType(),configuration);
         if (mv.isSupportInitializerExpression()) {
             JavaExpressionModel e = mv.getInitializerExpression();
             if (e!=null){
                if (CompileTimeConstants.DEBUG && debug) {
                    System.out.print("e: expr model =");
                    System.out.println(TermHelper.termToString(e.getModelTerm()));
                }
                markUsage(e,configuration,debug);
             }else{
                if (CompileTimeConstants.DEBUG && debug) {
                   System.out.println("e=null"); 
                }
             }         
         }else{
             if (CompileTimeConstants.DEBUG && debug) {
                System.out.println("mv does not support initialize expression");
             }
         }
     }
     //DEBUG
     if (debug) {
         System.out.println("nested types, size()="+typeModel.getNestedTypeModels().values());
     }               
     for(JavaTypeModel tm: typeModel.getNestedTypeModels().values()) {
         if (tm instanceof JavaTermTypeAbstractModel) {
           JavaTermTypeAbstractModel ttm = (JavaTermTypeAbstractModel)tm;  
           markExternal(ttm,configuration);
         }
     }     
   }
   
   public static void markUsage(JavaTopLevelBlockModel block, Configuration configuration, boolean debug) throws TermWareException, EntityNotFoundException
   {
     if (block.getStatements()!=null) {  
       for(JavaStatementModel st: block.getStatements()) {
          markUsage(st,configuration,debug);
       }
     }
   }
   
   public static void markUsage(JavaStatementModel st, Configuration configuration,boolean debug) throws TermWareException, EntityNotFoundException
   {       
      //System.out.println("markUsage: "+st.getKind());
      for(JavaLocalVariableModel vm: st.getLocalVariables()) {
          markExternal(vm.getType(),configuration);
          for(Map.Entry<String,JavaAnnotationInstanceModel> e:  vm.getAnnotationsMap().entrySet()) {
              markExternal(e.getValue().getAnnotationModel(),configuration);
          }
      }
      for(JavaExpressionModel e: st.getExpressions()) {
          markUsage(e,configuration,debug);
      }
      for(JavaStatementModel sti: st.getChildStatements()) {
          markUsage(sti,configuration,debug);
      }
   }
   
   public static void markUsage(JavaExpressionModel expr, Configuration configuration, boolean debug) throws TermWareException, EntityNotFoundException
   {
       if (CompileTimeConstants.DEBUG && debug){
           System.out.println("mark usage expression, kind="+expr.getKind());
       }
       switch(expr.getKind()) {
           case METHOD_CALL: 
           {
               JavaTermMethodCallExpressionModel e =(JavaTermMethodCallExpressionModel)expr;
               JavaMethodModel mm = e.getMethodModel();
               mm.setAttribute(REACHABLE_METHOD,TermUtils.createBoolean(true));
               markExternal(mm.getTypeModel(),configuration);
           }
           break;
           case FUNCTION_CALL:
           {
               JavaTermFunctionCallExpressionModel e = (JavaTermFunctionCallExpressionModel)expr;
               JavaMethodModel mm = e.getMethodModel();
               mm.setAttribute(REACHABLE_METHOD,TermUtils.createBoolean(true));
               markExternal(mm.getTypeModel(),configuration);
           }
           break;
           case SPECIALIZED_METHOD_CALL:
           {
               JavaTermSpecializedMethodCallExpressionModel e = (JavaTermSpecializedMethodCallExpressionModel)expr;
               JavaMethodModel mm = e.getMethodModel();
               mm.setAttribute(REACHABLE_METHOD,TermUtils.createBoolean(true));
               markExternal(mm.getTypeModel(),configuration);
           }
           break;                          
           case IDENTIFIER:
           {
               JavaTermIdentifierExpressionModel e = (JavaTermIdentifierExpressionModel)expr;
               if (!e.isType()) {
                   JavaVariableModel vm = e.getVariableModel();
                   switch(vm.getKind()) {
                       case FORMAL_PARAMETER:
                           break;
                       case LOCAL_VARIABLE:
                           break;
                       case MEMBER_VARIABLE:
                           vm.setAttribute(REACHABLE_FIELD,TermUtils.createBoolean(true));
                           markExternal(vm.getOwnerType(),configuration);
                           markExternal(vm.getType(),configuration);
                           break;
                       default:
                           throw new AssertException("Invalid variable kind:"+vm.getKind());
                   }                 
               }else{
                   markExternal(e.getType(),configuration);
               }
           }
           break;
           case FIELD:
           {
               JavaTermFieldExpressionModel e = (JavaTermFieldExpressionModel)expr;
               JavaMemberVariableModel vm = e.getFieldModel();
               vm.setAttribute(REACHABLE_FIELD,TermUtils.createBoolean(true));
               markExternal(vm.getOwnerType(),configuration);
               markExternal(vm.getType(),configuration);               
           }
           break;
           case STATIC_FIELD:
           {
               JavaTermStaticFieldExpressionModel e = (JavaTermStaticFieldExpressionModel)expr;
               JavaMemberVariableModel vm = e.getFieldModel();
               vm.setAttribute(REACHABLE_FIELD,TermUtils.createBoolean(true));
               markExternal(vm.getOwnerType(),configuration);
               markExternal(vm.getType(),configuration);
           }
           break;
           case ALLOCATION_EXPRESSION:
           {               
               JavaTermAllocationExpressionModel e = (JavaTermAllocationExpressionModel)expr;
               markExternal(e.getAllocatedType(),configuration);               
           }   
           break;
           case INNER_ALLOCATION:
           {
               JavaTermInnerAllocationExpressionModel e = (JavaTermInnerAllocationExpressionModel)expr;
               markExternal(e.getAllocatedType(),configuration);
           }
               break;
           case CAST:
           {
               JavaTermCastExpressionModel e = (JavaTermCastExpressionModel)expr;
               markExternal(e.getCastType(),configuration);
           }
           break;
           case CLASS_LITERAL:
           {
               JavaTermClassLiteralExpressionModel e = (JavaTermClassLiteralExpressionModel)expr;
               markExternal(e.getClassLiteralType(),configuration);
           }
           break;
           case INSTANCEOF:
           {
               JavaTermInstanceOfExpressionModel e = (JavaTermInstanceOfExpressionModel)expr;
               markExternal(e.getInstanceOfType(),configuration);
           }
           default:
               break;
       }
       for(JavaExpressionModel e: expr.getSubExpressions()) {
           markUsage(e,configuration,debug);
       }
   }
   
   public void printUnused(JavaTypeModel typeModel, JavaFacts facts) throws TermWareException, EntityNotFoundException
   {
       Collection<List<JavaMethodModel>> mmss = typeModel.getMethodModels().values();
       
       for(List<JavaMethodModel> mms: mmss) {
           for(JavaMethodModel mm: mms) {
               if (mm.getModifiers().isPrivate()) {                   
                 Term attr = mm.getAttribute(this.getClass().getName());               
                 if (attr.isNil()) {
                   if (mm instanceof JavaTermMethodModel) {
                       JavaTermMethodModel tmm = (JavaTermMethodModel)mm;
                       Term t = tmm.getTerm();
                       if (!isMethodOfSerializable(tmm)) {                                                  
                         facts.violationDiscovered("UnusedLocally","unused private method:"+mm.getName(),t);
                       }
                       mm.setAttribute("LocallyUnused",TermUtils.createBoolean(true));
                   }                     
                 }
               }
           }
       }
       
       Collection<JavaMemberVariableModel> vs = typeModel.getMemberVariableModels().values();
       
       for(JavaMemberVariableModel v: vs) {
           if (v.getModifiers().isPrivate()) {
               Term attr = v.getAttribute(this.getClass().getName());
               if (attr.isNil()) {
                   if (v instanceof JavaTermMemberVariableModel) {
                       JavaTermMemberVariableModel tv = (JavaTermMemberVariableModel)v;
                       if (!isFieldOfSerializable(tv)) {
                         Term t = tv.getVariableDeclaratorTerm();
                         facts.violationDiscovered("UnusedLocally","unused private field:"+v.getName(),t);
                       }
                   }
               }
           }
       }
       
   }
   
   private boolean isMethodOfSerializable(JavaTermMethodModel tmm) throws TermWareException, EntityNotFoundException
   {
      String name = tmm.getName();
      if (name.equals("writeObject")) {
          List<JavaTypeModel> fps = tmm.getFormalParametersTypes();
          if (fps.size()!=1) {
              return false;
          }
          JavaTypeModel fp = fps.get(0);
          if (JavaTypeModelHelper.same(fp,JavaResolver.resolveTypeModelByFullClassName("java.io.ObjectOutputStream"))){
              return JavaTypeModelHelper.subtypeOrSame(tmm.getTypeModel(),JavaResolver.resolveTypeModelByFullClassName("java.io.Serializable"));
          }                   
      }else if (name.equals("readObject")) {
          List<JavaTypeModel> fps = tmm.getFormalParametersTypes();
          if (fps.size()!=1) {
              return false;
          }
          JavaTypeModel fp = fps.get(0);
          if (JavaTypeModelHelper.same(fp,JavaResolver.resolveTypeModelByFullClassName("java.io.ObjectInputStream"))){
              return JavaTypeModelHelper.subtypeOrSame(tmm.getTypeModel(),JavaResolver.resolveTypeModelByFullClassName("java.io.Serializable"));
          }                             
      }else if (name.equals("readObjectNoData")) {
          List<JavaTypeModel> fps = tmm.getFormalParametersTypes();
          if (fps.size()!=0) {
              return false;
          }else{
              return JavaTypeModelHelper.subtypeOrSame(tmm.getTypeModel(),JavaResolver.resolveTypeModelByFullClassName("java.io.Serializable"));              
          }
      }
      return false;      
   }
   
   private boolean isFieldOfSerializable(JavaTermMemberVariableModel tv) throws TermWareException, EntityNotFoundException
   {
       if (tv.getName().equals("serialVersionUID")) {
           JavaModifiersModel m = tv.getModifiers();
           if (m.isFinal() && m.isStatic()) {
               return JavaTypeModelHelper.subtypeOrSame(tv.getOwner(),JavaResolver.resolveTypeModelByFullClassName("java.io.Serializable"));
           }
       }
       return false;
   }
    
    
}
