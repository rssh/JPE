package test;

public enum E2
{

  E2_FIRST(1),
  E2_SECOND(2),
  E2_THIRD(3)
  ;

  E2(int value)
  { value_=value; }

  public int getValue()
  { return value_; }

  private int value_;

}

