/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Global;

public class LockableGeneric<T>
{

	private final Object lock = new Object();
	private T V;

	public LockableGeneric(T V)
	{
		this.V = V;
	}

	public T get()
	{
		synchronized (lock)
		{
			return V;
		}
	}

	public void set(T var)
	{
		synchronized (lock)
		{
			this.V = var;
		}
	}

	public void inc(int x)
	{
		synchronized (lock)
		{
			if (V instanceof Integer)
			{
				var i = (Integer) V;
				i += x;
			}
		}
	}

	@Override
	public boolean equals(Object o)
	{
		synchronized (lock)
		{
			if (this == o)
				return true;

			if (!(o instanceof LockableGeneric))
				return false;

			var that = (LockableGeneric<?>) o;

			return V.equals(that.V);
		}
	}


}