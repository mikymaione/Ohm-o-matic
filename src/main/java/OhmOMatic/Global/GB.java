/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Global;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.TimerTask;

public final class GB
{

    private static Random random = new Random();


    public static byte[] sha1(String input)
    {
        try
        {
            var mDigest = MessageDigest.getInstance("SHA1");

            return mDigest.digest(input.getBytes());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();

            return null;
        }
    }

    public static boolean stringIsBlank(String s)
    {
        return (s == null || s.equals("") || s.equals(" "));
    }

    public static int randomInt(int da, int a)
    {
        return random.nextInt(a) + da;
    }

    public static boolean compreso(final byte[] key, final byte[] lower, final byte[] upper)
    {
        if (Arrays.compare(lower, upper) > 0)
            return Arrays.compare(key, lower) < 0 && Arrays.compare(key, upper) <= 1;
        else
            return Arrays.compare(key, lower) < 0 || Arrays.compare(key, upper) <= 1;
    }

    public static byte[] shiftLeft(byte[] byteArray, int shiftBitCount)
    {
        //https://github.com/patrickfav/bytes-java

        final var shiftMod = shiftBitCount % 8;
        final var carryMask = (byte) ((1 << shiftMod) - 1);
        final var offsetBytes = (shiftBitCount / 8);

        int sourceIndex;

        for (var i = 0; i < byteArray.length; i++)
        {
            sourceIndex = i + offsetBytes;

            if (sourceIndex >= byteArray.length)
            {
                byteArray[i] = 0;
            }
            else
            {
                final var src = byteArray[sourceIndex];
                var dst = (byte) (src << shiftMod);

                if (sourceIndex + 1 < byteArray.length)
                    dst |= byteArray[sourceIndex + 1] >>> (8 - shiftMod) & carryMask;

                byteArray[i] = dst;
            }
        }

        return byteArray;
    }

    public static TimerTask executeTimerTask(Runnable r)
    {
        return new TimerTask()
        {
            @Override
            public void run()
            {
                r.run();
            }
        };
    }


}