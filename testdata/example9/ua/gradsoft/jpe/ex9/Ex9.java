package ua.gradsoft.jpe.ex9;


public class Ex9
{

 public static void main(String[] args)
 {
   int x = 0x80000000;
   if (x!=0 && x == -x) {
     System.out.println("x==-x");
   }else{
     System.out.println("x!=-x");
   }
   
   
 }

}
