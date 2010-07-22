package test;

public class Ex7
{

 public static void main(String[] args)
 {
  Thread helloThread = new Thread() {
     public void run() {
       System.out.println("Something "+CompileTimeConstants.q1);
     }
  };
  helloThread.run();
 }


}
