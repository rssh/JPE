package test;

public enum E3
{

  E3_FIRST(1) {
    public int op(int x, int y)
     { return x+y; }
  },
  E3_SECOND(2) {
    public int op(int x, int y){
      return x-y;
    }
  },
  E3_THIRD(3) {
    public int op(int x, int y) {
      return x*y;
    }
  }
  ;

  E3(int value)
  { value_=value; }

  public abstract int op(int x, int y);

  public int getValue()
  { return value_; }

  private int value_;

}

