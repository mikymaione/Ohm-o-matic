/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


Java 11 version implemented from:
Chord: A Scalable Peer-to-peer Lookup Service for Internet Applications

Ion Stoica, Robert Morris, David Karger, M. Frans Kaashoek, Hari Balakrishnan
MIT Laboratory for Computer Science

chord@lcs.mit.edu
http://pdos.lcs.mit.edu/chord/
*/
package OhmOMatic.Sistema.Chord;

import OhmOMatic.Global.GB;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.*;


public class ChordNode implements Serializable
{

    private static final int TIMEOUT_STABILIZZAZIONE = 4000;
    private static final int FATTORE_REPLICA = 2;
    private static final int TIMEOUT_PEER = 500;

    private static final int mBit = 4;


    private final String ID;
    private final byte[] key;

    private final HashMap<byte[], Serializable> data = new HashMap<>();
    private final ChordNode[] fingers = new ChordNode[mBit];

    private volatile Deque<ChordNode> successors = new ArrayDeque<>();
    private volatile ChordNode predecessor;


    public ChordNode(final String ID_)
    {
        ID = ID_;
        key = GB.sha1(ID_);
        setSuccessor(this);

        //var executor = Executors.newSingleThreadScheduledExecutor();
        //executor.scheduleAtFixedRate(GB.executeTimerTask(this::stabilize), TIMEOUT_STABILIZZAZIONE, TIMEOUT_STABILIZZAZIONE, TimeUnit.MILLISECONDS);
    }


    //region Funzioni interne di gestione
    private static boolean isAlive(final ChordNode n)
    {
        try
        {
            var t = new FutureTask<>(() -> n.key);

            new Thread(t).start();

            t.get(TIMEOUT_PEER, TimeUnit.MILLISECONDS);

            return true;
        }
        catch (InterruptedException | ExecutionException | TimeoutException ex)
        {
            return false;
        }
    }

    private void setSuccessor(final ChordNode n)
    {
        synchronized (fingers)
        {
            fingers[0] = n;
        }
    }

    private void setPredecessor(final ChordNode n)
    {
        predecessor = n;
    }

    private ChordNode closest_preceding_finger(final byte[] id)
    {
        var n = this;

        synchronized (fingers)
        {
            for (var f : fingers)
            {
                if (!isAlive(f))
                    continue;

                if (GB.compreso(f.key, n.key, id))
                    n = f;
            }
        }

        return n;
    }

    public void join(final ChordNode n)
    {
        setSuccessor(n.find_successor(key));
    }

    private void stabilize()
    {
        var n = this;
        var s = successor();
        var x = s.predecessor();

        if (x != null && GB.compreso(x.key, n.key, s.key))
            setSuccessor(x);

        s = successor();
        s.notify(n);

        fixFingers();
        handoff();
        reconsileSuccessors();
    }

    private void fixFingers()
    {
        synchronized (fingers)
        {
            for (var i = 1; i < fingers.length; i++)
                fingers[i] = find_successor(GB.shiftLeft(key, i));
        }
    }

    private void handoff()
    {
        synchronized (data)
        {
            for (var aKey : data.keySet())
            {
                var s = find_successor(aKey);

                if (!Arrays.equals(key, s.key))
                    s.offer(aKey, data.remove(aKey));
            }
        }
    }

    private void reconsileSuccessors()
    {
        var n = this;
        var s = successor();

        if (s == n)
            return;

        var successors = s.successors();

        successors.addFirst(s);

        if (successors.size() > FATTORE_REPLICA)
            successors.removeLast();

        n.successors = successors;
    }
    //endregion

    //region Funzioni esterne
    public ChordNode successor()
    {
        if (!isAlive(fingers[0]))
        {
            synchronized (successors)
            {
                var first = true;

                for (var s : successors)
                {
                    if (!first && isAlive(s))
                    {
                        setSuccessor(s);
                        break;
                    }

                    if (first)
                        first = false;
                }
            }

            reconsileSuccessors();
        }

        return fingers[0];
    }

    public ChordNode find_successor(final byte[] id)
    {
        var n = this;
        var n_ = successor();

        if (GB.compreso(id, n.key, n_.key))
            return n_;

        var f = closest_preceding_finger(id);

        if (f == n)
            return n;
        else
            return f.find_successor(id);
    }

    public Deque<ChordNode> successors()
    {
        return successors;
    }

    public ChordNode predecessor()
    {
        if (predecessor != null && !isAlive(predecessor))
            predecessor = null;

        return predecessor;
    }

    public void notify(final ChordNode n)
    {
        var p = predecessor();

        if (p == null || GB.compreso(n.key, p.key, key))
            setPredecessor(n);
    }

    public void offer(final byte[] id, final Serializable object)
    {
        synchronized (data)
        {
            if (!data.containsKey(id))
                data.put(id, object);
        }
    }

    public void printData()
    {
        var s = predecessor();
        var n = s;

        if (s == null)
            s = this;

        GB.clearScreen();

        do
        {
            System.out.println("Nodo: " + s.ID);

            for (var d : s.data.entrySet())
                System.out.println("-Energia consumata " + d.getKey() + ": " + d.getValue() + "KW");

            n = s;
            s = successor();

            if (s == null)
                break;
        }
        while (!Arrays.equals(n.key, s.key));
    }

    public <T extends Serializable> T get(final byte[] id)
    {
        var s = find_successor(id);

        if (Arrays.equals(key, s.key))
            synchronized (data)
            {
                return (T) data.get(id);
            }
        else
            return (T) s.get(id);
    }

    public <T extends Serializable> T put(final byte[] id, final Serializable object)
    {
        var s = find_successor(id);

        if (Arrays.equals(key, s.key))
            synchronized (data)
            {
                return (T) data.put(id, object);
            }
        else
            return (T) s.put(id, object);
    }
    //endregion


}