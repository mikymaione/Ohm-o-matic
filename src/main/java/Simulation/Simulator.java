package Simulation;

import java.util.Calendar;
import java.util.Random;

public abstract class Simulator extends Thread
{
    protected volatile boolean stopCondition = false;
    protected Random rnd = new Random();

    private long midnight;
    private Buffer buffer;
    private String id;
    private String type;

    public abstract void run();


    public Simulator(String id, String type, Buffer buffer)
    {
        this.id = id;
        this.type = type;
        this.buffer = buffer;
        this.midnight = computeMidnightMilliseconds();
    }

    public void stopMeGently()
    {
        stopCondition = true;
    }

    protected void addMeasurement(double measurement)
    {
        buffer.addMeasurement(new Measurement(id, type, measurement, deltaTime()));
    }

    public Buffer getBuffer()
    {
        return buffer;
    }

    protected void sensorSleep(long milliseconds)
    {
        try
        {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private long computeMidnightMilliseconds()
    {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis();
    }

    private long deltaTime()
    {
        return System.currentTimeMillis() - midnight;
    }

    public String getIdentifier()
    {
        return id;
    }

}