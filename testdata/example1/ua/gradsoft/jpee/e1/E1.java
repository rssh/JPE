package ua.gradsoft.jpee.e1;


public class E1
{

  public static void main(String[] args)
  {
    if (CompileTimeConstants.DEBUG) {
      System.out.println("enter main");
    }
    System.out.println("E1:Hello,world");
    if (CompileTimeConstants.DEBUG) {
      System.out.println("leave main");
    }
  }

}