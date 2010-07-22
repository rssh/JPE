package test;

import static test.CompileTimeConstants.*;

public class Test
{

 public static void main(String[] args)
 {
   E1 e1 = E1.E1_FIRST;
   E2 e2 = E2.E2_FIRST;
   E3 e3 = E3.E3_FIRST;
   int q = q1;
   if (q1==1) {
     System.out.println("AAA");
   }else if (q1+3 < 5) {
     System.out.println("BBB");
   }else{
     System.out.println("CCC");
   }
 }

}
