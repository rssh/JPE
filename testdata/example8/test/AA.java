package test;

public abstract class AA implements IA
{

  public void  f2()
  {
    try {
      f1();
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }

  public void  f3()
   {}

}
