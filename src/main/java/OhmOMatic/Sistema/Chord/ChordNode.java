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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public final class ChordNode implements Serializable
{

    private static final long serialVersionUID = 987654321;

    private static final int TIMEOUT_STABILIZZAZIONE = 4000;
    private static final int FATTORE_REPLICA = 2;
    private static final int TIMEOUT_PEER = 500;

    private static final int mBit = 128;


    private final byte[] key;

    private final HashMap<byte[], Serializable> data = new HashMap<>();
    private final ChordNode[] fingers = new ChordNode[mBit];

    private volatile Deque<ChordNode> successors = new ArrayDeque<>();
    private volatile ChordNode predecessor;

    private Timer timer = new Timer();


    public ChordNode(final String host)
    {
        key = GB.sha1(host);
        setSuccessor(this);

        //var stub = (ChordNode) UnicastRemoteObject.exportObject(this, 0);
        //this.channel = new Channel(host, c -> c.write(stub));

        timer.schedule(GB.executeTimerTask(this::stabilize), TIMEOUT_STABILIZZAZIONE);
    }

    public ChordNode(final String host, final ChordNode known)
    {
        this(host);
        join(known);
    }

    public ChordNode(final String host, final String peer)
    {
        key = null;
        //this(host, (ChordNode) Proxy.connect(peer));
    }

    //region Funzioni interne di gestione
    private static boolean isAlive(final ChordNode chordNode)
    {
        try
        {
            var t = new FutureTask<>(() -> chordNode.key);

            new Thread(t).start();

            t.get(TIMEOUT_PEER, TimeUnit.MILLISECONDS);

            return true;
        }
        catch (InterruptedException | ExecutionException | TimeoutException ex)
        {
            return false;
        }
    }

    private void setSuccessor(final ChordNode chordNode)
    {
        synchronized (fingers)
        {
            fingers[0] = chordNode;
        }
    }

    private void setPredecessor(final ChordNode chordNode)
    {
        predecessor = chordNode;
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

    private void join(final ChordNode chordNode)
    {
        setSuccessor(chordNode.find_successor(key));
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

    public void notify(final ChordNode chordNode)
    {
        var n = this;
        var p = predecessor();

        if (p == null || GB.compreso(chordNode.key, p.key, n.key))
            setPredecessor(chordNode);
    }

    public void offer(final byte[] aKey, final Serializable object)
    {
        synchronized (data)
        {
            if (!data.containsKey(aKey))
                data.put(aKey, object);
        }
    }

    public <T extends Serializable> T get(final byte[] aKey)
    {
        var s = find_successor(aKey);

        if (Arrays.equals(key, s.key))
            synchronized (data)
            {
                return (T) data.get(aKey);
            }
        else
            return (T) s.get(aKey);
    }

    public <T extends Serializable> T put(final byte[] aKey, final Serializable object)
    {
        var s = find_successor(aKey);

        if (Arrays.equals(key, s.key))
            synchronized (data)
            {
                return (T) data.put(aKey, object);
            }
        else
            return (T) s.put(aKey, object);
    }
    //endregion


}