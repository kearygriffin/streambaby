package org.arabidopsis.interval;

/*
Copyright (c) 2006, Danny Yoo (dyoo@hkn.eecs.berkeley.edu)

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
    * Neither the name of the Carnegie Institution of Washington nor
the names of its contributors may be used to endorse or promote
products derived from this software without specific prior written
permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
/** An implementation of an interval tree, following the explanation.
 * from CLR.
 */


import java.util.WeakHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class IntervalTree {
    private StatisticUpdate updater;
    private RbTree tree;

    private Map intervals;
    private Map max;
    private Map min;

	public IntervalTree() {
	this.updater = new IntervalTreeStatisticUpdate();
	this.tree = new RbTree(this.updater);

	this.intervals = new WeakHashMap();
	this.intervals.put(RbNode.NIL, null);

	this.max = new WeakHashMap();
	this.max.put(RbNode.NIL, new Double(Double.MIN_VALUE));
	this.min = new WeakHashMap();
	this.min.put(RbNode.NIL, new Double(Double.MAX_VALUE));
    }


    public void insert(Interval interval) {
	RbNode node = new RbNode(interval.getLow());
	this.intervals.put(node, interval);
	this.tree.insert(node);
    }



    public int size() {
	return this.tree.size();
    }



    // Returns the first matching interval that we can find.
    public Interval search(Interval interval) {

	RbNode node = tree.root();
	if (node.isNull())
	    return null;

	while ( (! node.isNull()) &&
		(! getInterval(node).overlaps(interval))) {
	    if (canOverlapOnLeftSide(interval, node)) {
		node = node.left;
	    } else if (canOverlapOnRightSide(interval, node)) {
		node = node.right;
	    } else {
		return null;
	    }
	}

	// Defensive coding.  node can be the NIL node, but it must
	// not be itself the null object.
	assert node != null;
	return getInterval(node);
    }


    private boolean canOverlapOnLeftSide(Interval interval,
					 RbNode node) {
	return (! node.left.isNull()) &&
	    getMax(node.left) >= interval.getLow();
    }


    private boolean canOverlapOnRightSide(Interval interval,
					 RbNode node) {
	return (! node.right.isNull()) &&
	    getMin(node.right) <= interval.getHigh();
    }



    // Returns all matches as a list of Intervals
    public List searchAll(Interval interval) {

	if (tree.root().isNull()) {
	    return new ArrayList();
	}
	return this._searchAll(interval, tree.root());
    }


    private List _searchAll(Interval interval, RbNode node) {
	assert (! node.isNull());


	List results = new ArrayList();
	if (getInterval(node).overlaps(interval)) {
	    results.add(getInterval(node));
	} else {
	}

	if (canOverlapOnLeftSide(interval, node)) {
	    results.addAll(_searchAll(interval, node.left));
	}

	if (canOverlapOnRightSide(interval, node)) {
	    results.addAll(_searchAll(interval, node.right));
	}

	return results;
    }



    
    public Interval getInterval(RbNode node) {
	assert (node != null);
	assert (! node.isNull());

	assert (this.intervals.containsKey(node));

	return (Interval) this.intervals.get(node);
    }


    public double  getMax(RbNode node) {
	assert (node != null);
	assert (this.intervals.containsKey(node));

	return ((Double) this.max.get(node)).doubleValue();
    }


    private void setMax(RbNode node, double value) {
	this.max.put(node, new Double(value));
    }


    public double getMin(RbNode node) {
	assert (node != null);
	assert (this.intervals.containsKey(node));

	return ((Double) this.min.get(node)).doubleValue();
    }


    private void setMin(RbNode node, double value) {
	this.min.put(node, new Double(value));
    }



    private class IntervalTreeStatisticUpdate 
	implements StatisticUpdate {
	public void update(RbNode node) {
	    setMax(node, max(max(getMax(node.left),
				 getMax(node.right)),
			     getInterval(node).getHigh()));

	    setMin(node, min(min(getMin(node.left),
				 getMin(node.right)),
			     getInterval(node).getLow()));
	}


	private double max(double x, double y) {
	    if (x > y) { return x; }
	    return y;
	}

	private double min(double x, double y) {
	    if (x < y) { return x; }
	    return y;
	}


    }








    /**
     *
     * Test case code: check to see that the data structure follows
     * the right constraints of interval trees:
     *
     *     o.  They're valid red-black trees
     *     o.  getMax(node) is the maximum of any interval rooted at that node..
     *
     * This code is expensive, and only meant to be used for
     * assertions and testing.
     */
    public boolean isValid() {
	return (this.tree.isValid() && 
		hasCorrectMaxFields(this.tree.root) &&
		hasCorrectMinFields(this.tree.root));
    }


    private boolean hasCorrectMaxFields(RbNode node) {
	if (node.isNull())
	    return true;
	return (getRealMax(node) == getMax(node) &&
		hasCorrectMaxFields(node.left) &&
		hasCorrectMaxFields(node.right));
    }


    private boolean hasCorrectMinFields(RbNode node) {
	if (node.isNull())
	    return true;
	return (getRealMin(node) == getMin(node) &&
		hasCorrectMinFields(node.left) &&
		hasCorrectMinFields(node.right));
    }


    private double getRealMax(RbNode node) {
	if (node.isNull())
	    return Double.MIN_VALUE;	
	double leftMax = getRealMax(node.left);
	double rightMax = getRealMax(node.right);
	double nodeHigh = getInterval(node).getHigh();
	
	double max1 = (leftMax > rightMax ? leftMax : rightMax);
	return (max1 > nodeHigh ? max1 : nodeHigh);
    }


    private double getRealMin(RbNode node) {
	if (node.isNull())
	    return Double.MAX_VALUE;	

	double leftMin = getRealMin(node.left);
	double rightMin = getRealMin(node.right);
	double nodeLow = getInterval(node).getLow();
	
	double min1 = (leftMin < rightMin ? leftMin : rightMin);
	return (min1 < nodeLow ? min1 : nodeLow);
    }


}
