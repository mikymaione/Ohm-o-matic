/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Global;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Waiter
{

	private final Timer timer;
	private final Runnable func;

	private Boolean inEsecuzione = false;
	private Boolean elaborazioneInCorso = false;

	private final Object _inEsecuzioneLock = new Object();
	private final Object _elaborazioneInCorsoLock = new Object();

	private final long millisecondi;


	public Waiter(String nome, Runnable func, long millisecondi)
	{
		this.func = func;
		this.millisecondi = millisecondi;
		this.timer = new Timer("__" + nome);
	}

	public void start()
	{
		synchronized (_inEsecuzioneLock)
		{
			if (!inEsecuzione)
			{
				inEsecuzione = true;
				timer.scheduleAtFixedRate(new TimerTask()
				{
					@Override
					public void run()
					{
						esegui();
					}
				}, new Date(), millisecondi);
			}
		}
	}

	private void esegui()
	{
		synchronized (_inEsecuzioneLock)
		{
			synchronized (_elaborazioneInCorsoLock)
			{
				if (inEsecuzione && !elaborazioneInCorso)
				{
					elaborazioneInCorso = true;
					func.run();
					elaborazioneInCorso = false;
				}
			}
		}
	}

	public Boolean isRunning()
	{
		synchronized (_elaborazioneInCorsoLock)
		{
			return elaborazioneInCorso;
		}
	}

	public void stopMeGently()
	{
		synchronized (_inEsecuzioneLock)
		{
			timer.cancel();
			inEsecuzione = false;
		}
	}


}