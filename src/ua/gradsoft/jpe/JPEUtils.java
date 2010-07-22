/*
 * JPEUtils.java
 *
 */

package ua.gradsoft.jpe;

import ua.gradsoft.javachecker.models.JavaPackageModel;
import ua.gradsoft.javachecker.models.JavaTypeModel;
import ua.gradsoft.termware.Term;
import ua.gradsoft.termware.TermWare;
import ua.gradsoft.termware.TermWareException;

/**
 *Static class for utilities
 * @author rssh
 */
public class JPEUtils {
    
    
    public static Term createPackageTerm(JavaTypeModel tpm) throws TermWareException
    {
        JavaPackageModel jp = tpm.getPackageModel();
        String nameWithDots = jp.getName();
        String[] nameComponents = nameWithDots.split("\\.");
        Term[] tncs = new Term[nameComponents.length];
        for(int i=0; i<nameComponents.length; ++i) {
            Term st = TermWare.getInstance().getTermFactory().createString(nameComponents[i]);
            tncs[i] = TermWare.getInstance().getTermFactory().createTerm("Identifier",st);
        }
        Term nl = TermWare.getInstance().getTermFactory().createList(tncs);
        Term nt = TermWare.getInstance().getTermFactory().createTerm("Name",nl);
        Term retval = TermWare.getInstance().getTermFactory().createTerm("PackageDeclaration",nt);
        return retval;
    }
    
    
}
