package test;

public enum E1
{

  E1_FIRST(1),
  E1_SECOND(2),
  E1_THIRD(3)
  ;

  E1(int value)
  { value_=value; }

  public int getValue()
  { return value_; }

  private int value_;

}

