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

import java.security.NoSuchAlgorithmException;

public class ChordNode
{

    private final int mBit;
    private final byte[] ID;
    private final String localURL;

    private ChordNode node, predecessor, successor;
    private ChordNode[] finger;

    private byte[] start;


    public ChordNode(int N_Bit, String localURL) throws NoSuchAlgorithmException
    {
        this.localURL = localURL;
        this.mBit = N_Bit;

        this.ID = GB.sha1(localURL);
        this.finger = new ChordNode[getLengthOfID()];
    }

    private int getLengthOfID()
    {
        return this.ID.length * 8;
    }

    private ChordNode find_successor(byte[] id)
    {
        var n_ = find_predecessor(id);

        return n_.successor;
    }

    private ChordNode find_predecessor(byte[] id)
    {
        var n = this;
        var n_ = n;

        while (!appartiene(id, new ChordNode[]{n_, n_.successor}))
            n_ = n_.closest_preceding_finger(id);

        return n_;
    }

    private ChordNode closest_preceding_finger(byte[] id)
    {
        var n = this;

        for (var i = mBit - 1; i-- > 0; )
            if (appartiene(finger[i].node.ID, new byte[][]{n.ID, id}))
                return finger[i].node;

        return n;
    }

    private void join(ChordNode n_)
    {
        var n = this;

        if (n_ == null)
        {
            //sei il primo nodo
            for (var i = 0; i < mBit; i++)
                finger[i].node = n;

            predecessor = n;
        }
        else
        {
            init_finger_table(n_);
            update_others();
        }
    }

    private void stabilize()
    {
        var n = this;
        var x = successor.predecessor;

        if (appartiene(x, new ChordNode[]{n, successor}))
            successor = x;

        successor.notify(n);
    }

    private void notify(ChordNode n_)
    {
        var n = this;

        if (predecessor == null || appartiene(n_, new ChordNode[]{predecessor, n}))
            predecessor = n_;
    }

    private void fix_fingers()
    {
        var i = GB.RandomInt(1, finger.length - 1);

        finger[i].node = find_successor(finger[i].start);
    }

    private void update_others()
    {
        var n = this;

        for (var i = 0; i < mBit; i++)
        {
            var p = find_predecessor(null);
            p.update_finger_table(n, i);
        }
    }

    private void update_finger_table(ChordNode s, int i)
    {
        var n = this;

        if (appartiene(s, new ChordNode[]{n, finger[i].node}))
        {
            finger[i].node = s;
            var p = predecessor;
            p.update_finger_table(s, i);
        }
    }

    private void init_finger_table(ChordNode n_)
    {
        var n = this;

        finger[0].node = n_.find_successor(finger[0].start);
        predecessor = successor.predecessor;
        successor.predecessor = n;

        for (var i = 0; i < mBit - 1; i++)
            if (appartiene(finger[i + 1].start, new ChordNode[]{n, finger[i].node}))
                finger[i + 1].node = finger[i].node;
            else
                finger[i + 1].node = n_.find_successor(finger[i + 1].start);
    }


    //region Funzioni di utilità
    private static final boolean appartiene(byte[] an_id, byte[][] elementi)
    {
        for (var e : elementi)
            if (an_id.equals(e))
                return true;

        return false;
    }

    private static final boolean appartiene(ChordNode a_node, ChordNode[] elementi)
    {
        return appartiene(a_node.ID, elementi);
    }

    private static final boolean appartiene(byte[] an_id, ChordNode[] elementi)
    {
        final var l = elementi.length;
        var A = new byte[l][];

        for (var i = 0; i < l; i++)
            A[i] = elementi[i].ID;

        return appartiene(an_id, A);
    }
    //endregion

}