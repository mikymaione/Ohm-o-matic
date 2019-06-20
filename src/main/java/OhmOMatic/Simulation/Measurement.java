/*
MIT License
Copyright (c) 2019 Gabriele Civitarese
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Simulation;

public class Measurement implements Comparable<Measurement>
{
	private String id;
	private String type;
	private double value;
	private long timestamp;


	public Measurement(String id, String type, double value, long timestamp)
	{
		this.value = value;
		this.timestamp = timestamp;
		this.id = id;
		this.type = type;
	}


	public double getValue()
	{
		return value;
	}

	public void setValue(double value)
	{
		this.value = value;
	}


	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}


	public String getId()
	{
		return id;
	}

	public void setId(String type)
	{
		this.id = type;
	}


	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}


	@Override
	public int compareTo(Measurement m)
	{
		Long thisTimestamp = timestamp;
		Long otherTimestamp = m.getTimestamp();

		return thisTimestamp.compareTo(otherTimestamp);
	}

	public String toString()
	{
		return value + " " + timestamp;
	}

}