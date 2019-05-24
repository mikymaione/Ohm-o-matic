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
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public final class ChordNode implements Serializable
{

    private static final long serialVersionUID = 987654321;

    private static final int STABILIZATION_INTERVAL = 4000;
    private static final int REPLICATION_FACTOR = 2;
    private static final int PEER_TIMEOUT = 500;

    private static final int mBit = 128;


    private final byte[] key;

    private final HashMap<byte[], Serializable> data = new HashMap<>();
    private final ChordNode[] fingers = new ChordNode[mBit];

    private volatile Deque<ChordNode> successors = new ArrayDeque<>();
    private volatile ChordNode predecessor;

    private Timer timer = new Timer();


    public ChordNode(final String host) throws NoSuchAlgorithmException
    {
        this.key = GB.sha1(host);
        this.successor(this);

        //var stub = (ChordNode) UnicastRemoteObject.exportObject(this, 0);
        //this.channel = new Channel(host, c -> c.write(stub));

        timer.schedule(GB.executeTimerTask(this::stabilize), STABILIZATION_INTERVAL);
    }

    public ChordNode(final String host, final ChordNode known) throws NoSuchAlgorithmException
    {
        this(host);
        this.join(known);
    }

    public ChordNode(final String host, final String peer)
    {
        key = null;
        //this(host, (ChordNode) Proxy.connect(peer));
    }


    public ChordNode successor()
    {
        if (!isAlive(fingers[0]))
        {
            synchronized (successors)
            {
                this.successor(
                        successors.stream()
                                .skip(1)
                                .filter(ChordNode::isAlive)
                                .findFirst()
                                .orElse(this)
                );
            }

            reconsileSuccessors();
        }

        return fingers[0];
    }

    private void successor(final ChordNode chordNode)
    {
        synchronized (fingers)
        {
            fingers[0] = chordNode;
        }
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

    private void predecessor(final ChordNode chordNode)
    {
        predecessor = chordNode;
    }

    public ChordNode findSuccessor(final byte[] key)
    {
        var s = this.successor();

        if (GB.compreso(key, this.key, s.key))
            return s;

        var c = this.closest(key);

        if (c == this)
            return this;
        else
            return c.findSuccessor(key);
    }

    public void notify(final ChordNode chordNode)
    {
        var p = this.predecessor();

        if (p == null)
            this.predecessor(chordNode);
        else if (chordNode == this)
            return;
        else if (GB.compreso(chordNode.key, p.key, this.key))
            this.predecessor(chordNode);
    }

    public <T extends Serializable> T get(final byte[] key)
    {
        var r = this.findSuccessor(key);

        if (Arrays.equals(this.key, r.key))
            synchronized (this.data)
            {
                return (T) this.data.get(key);
            }
        else
            return (T) r.get(key);
    }

    public <T extends Serializable> T put(final byte[] key, final Serializable object)
    {
        var r = this.findSuccessor(key);

        if (Arrays.equals(this.key, r.key))
            synchronized (this.data)
            {
                return (T) this.data.put(key, object);
            }
        else
            return (T) r.put(key, object);
    }

    public void offer(final byte[] key, final Serializable object)
    {
        synchronized (this.data)
        {
            if (!this.data.containsKey(key))
                this.data.put(key, object);
        }
    }

    private ChordNode closest(final byte[] key)
    {
        var c = this;

        synchronized (this.fingers)
        {
            for (var n : this.fingers)
            {
                if (!isAlive(n))
                    continue;

                if (GB.compreso(n.key, this.key, key))
                    c = n;
            }
        }

        return c;
    }

    private void join(final ChordNode chordNode)
    {
        successor(chordNode.findSuccessor(this.key));
    }

    private void stabilize()
    {
        var s = successor();
        var c = s.predecessor();

        if (c != null && GB.compreso(c.key, key, s.key))
            successor(c);

        successor().notify(this);

        fixFingers();
        handoff();
        reconsileSuccessors();
    }

    private void fixFingers()
    {
        synchronized (fingers)
        {
            for (var bits = 1; bits < fingers.length; bits++)
                fingers[bits] = findSuccessor(GB.shiftLeft(key, bits));
        }
    }

    private void handoff()
    {
        synchronized (data)
        {
            for (var k : data.keySet())
            {
                var s = findSuccessor(k);

                if (!Arrays.equals(key, s.key))
                    s.offer(k, data.remove(k));
            }
        }
    }

    private void reconsileSuccessors()
    {
        var s = successor();

        if (s == this)
            return;

        var successors = s.successors();

        successors.addFirst(s);

        if (successors.size() > REPLICATION_FACTOR)
            successors.removeLast();

        this.successors = successors;
    }

    private static boolean isAlive(final ChordNode chordNode)
    {
        try
        {
            var future = new FutureTask<>(() -> chordNode.key);

            new Thread(future).start();

            future.get(PEER_TIMEOUT, TimeUnit.MILLISECONDS);

            return true;
        }
        catch (InterruptedException | ExecutionException | TimeoutException ex)
        {
            return false;
        }
    }


}