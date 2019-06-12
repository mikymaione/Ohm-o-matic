/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord;

import OhmOMatic.Global.GB;

public class Chord
{
    private final int mBit = 160; //sha1
    private byte[] key;
    private Chord predecessor, successor;

    private volatile Chord[] finger = new Chord[mBit];
    private int next;

    // create a new Chord ring.
    public Chord(byte[] key_)
    {
        key = key_;
        predecessor = null;
        successor = this;
    }

    // ask node n to find the successor of id
    private Chord find_successor(byte[] key)
    {
        if (GB.compreso(key, this.key, successor.key))
            return successor;
        else
        {
            var n0 = closest_preceding_node(key);

            return n0.find_successor(key);
        }
    }

    // search the local table for the highest predecessor of id
    private Chord closest_preceding_node(byte[] key)
    {
        for (var i = mBit - 1; i-- > 0; )
            if (GB.compreso(finger[i].key, this.key, key))
                return finger[i];

        return this;
    }

    // join a Chord ring containing node n_
    private void join(Chord n_)
    {
        predecessor = null;
        successor = n_.find_successor(this.key);
    }

    // called periodically. n asks the successor
    // about its predecessor, verifies if n's immediate
    // successor is consistent, and tells the successor about n
    private void stabilize()
    {
        var x = successor.predecessor;

        if (GB.compreso(x.key, this.key, successor.key))
            successor = x;

        successor.notify(this);
    }

    // n_ thinks it might be our predecessor.
    private void notify(Chord n_)
    {
        if (predecessor == null || GB.compreso(n_.key, predecessor.key, this.key))
            predecessor = n_;
    }

    // called periodically. refreshes finger table entries.
    // next stores the index of the finger to fix
    private void fix_fingers()
    {
        next = next + 1;

        if (next > mBit)
            next = 1;

        finger[next] = find_successor(null);
    }

    // called periodically. checks whether predecessor has failed.
    private void check_predecessor()
    {
        if (isActive(predecessor))
            predecessor = null;
    }

    private boolean isActive(Chord e)
    {
        return true;
    }


}