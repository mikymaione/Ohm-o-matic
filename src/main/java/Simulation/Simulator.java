package Simulation;

import com.modeliosoft.modelio.javadesigner.annotations.objid;

import java.util.Calendar;
import java.util.Random;

@objid("0df2e587-96f1-4bc8-8a13-fd974b7ced73")
public abstract class Simulator extends Thread
{
    @objid("1677fe60-7073-467d-a8d7-b48680f3be25")
    protected volatile boolean stopCondition = false;

    @objid("1af75439-4336-4d03-8032-fed327695737")
    private long midnight;

    @objid("a4e1a88a-64b9-4988-9d30-26f38f5ecbb3")
    private String id;

    @objid("07a2ac7e-7b4f-4f7d-be33-85a1190d235e")
    private String type;

    @objid("e833faf5-11ce-4880-8c8f-edcc60bae419")
    protected Random rnd = new Random();

    @objid("52ced4b5-7cfc-47d9-86e4-b66a0260ed47")
    private Buffer buffer;

    @objid("a787e0c9-cbde-4ce0-9b88-1a3241df813d")
    public abstract void run();

    @objid("d9d0601b-fbc1-48ec-94be-885990f4b7d5")
    public Simulator(String id, String type, Buffer buffer)
    {
        this.id = id;
        this.type = type;
        this.buffer = buffer;
        this.midnight = computeMidnightMilliseconds();
    }

    @objid("d386bf2d-0628-4430-893e-27c31b98c0fe")
    public void stopMeGently()
    {
        stopCondition = true;
    }

    @objid("9e67f694-a907-437e-8234-72a6b7813d7a")
    protected void addMeasurement(double measurement)
    {
        buffer.addMeasurement(new Measurement(id, type, measurement, deltaTime()));
    }

    @objid("4d3a443f-9214-4ed0-994d-70a25be1f5d1")
    public Buffer getBuffer()
    {
        return buffer;
    }

    @objid("94eddc11-2806-485d-98fc-38567ef570ed")
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

    @objid("34d02427-a632-44bb-8cd4-1c68f6ea5ca3")
    private long computeMidnightMilliseconds()
    {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    @objid("42ee7f07-42dd-431e-ae88-cceeb17db86b")
    private long deltaTime()
    {
        return System.currentTimeMillis() - midnight;
    }

    @objid("7aeb630b-9b70-46e8-88de-f16e1a164ccc")
    public String getIdentifier()
    {
        return id;
    }

}
