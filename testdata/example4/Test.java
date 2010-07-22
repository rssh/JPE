package test;

import ua.gradsoft.termware.*;
import java.util.*;
import java.lang.*;
import ua.gradsoft.termware.strategies.FirstTopStrategy;
//comment1
public class Test
{
//comment2
	public Test()
	{

	}
	public void TestMethod(Object o)
	{
		if (!(o instanceof Term))
		{
			return;
			//comment3
		}

	}
	public float TestFloating(boolean flag)
	{
		float tmp = -1.0;
		//return flag ? 1.0 : 0.0;
	}
	public void TestShift(int n)
	{
		int i = n >> 1;
		int j = n << 1;
		
	}
	class Inner
	{
		//comment4
		public void innerTest()
		{
			//comment5
			Test.this.TestFloating(false);
		}

	}

}
