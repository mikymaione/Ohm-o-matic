package Simulation;

public class SmartMeterSimulator extends Simulator
{
    private final double A = 0.4;
    private final double W = 0.01;

    private static int ID = 1;

    private volatile boolean boost;

    public SmartMeterSimulator(String id, Buffer buffer)
    {
        super(id, "Plug", buffer);
    }

    public SmartMeterSimulator(Buffer buffer)
    {
        super("plug-" + (ID++), "Plug", buffer);
    }

    @Override
    public void run()
    {
        double i = rnd.nextInt();
        long waitingTime;

        while (!stopCondition)
        {
            double value = getElecticityValue(i);

            if (boost)
                value += 3;

            addMeasurement(value);

            waitingTime = 100 + (int) (Math.random() * 200);
            sensorSleep(waitingTime);

            i += 0.2;
        }
    }

    private double getElecticityValue(double t)
    {
        return Math.abs(A * Math.sin(W * t) + rnd.nextGaussian() * 0.3);
    }

    public void boost() throws InterruptedException
    {
        boost = true;

        Thread.sleep(5000);

        boost = false;
    }

}