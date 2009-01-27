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

//Quick and dirty interval class
@SuppressWarnings("unchecked")
public class Interval implements Comparable {
 private final double low;
 private final double high;

 public Interval(double low, double high) {
	assert low <= high;
	this.low = low;
	this.high = high;
 }


 public boolean equals(Object other) {
	if (this == other)
	    return true;
	if (this.getClass().equals(other.getClass())) {
	    Interval otherInterval = (Interval) other;
	    return (this.low == otherInterval.low &&
		    this.high == otherInterval.high);
	}
	return false;
 }


 public int hashCode() {
	return new Double(low).hashCode();
 }


 public int compareTo(Object o) {
	Interval other = (Interval) o;
	if (this.low < other.low)
	    return -1;
	if (this.low > other.low)
	    return 1;

	if (this.high < other.high)
	    return -1;
	if (this.high > other.high)
	    return 1;

	return 0;
 }

 public String toString() {
	return "Interval[" + this.low + ", " + this.high + "]";
 }


 /**
  * Returns true if this interval overlaps the other.
  */
 public boolean overlaps(Interval other) {
	return (this.low <= other.high &&
		other.low <= this.high);
 }


 public double getLow() {
	return this.low;
 }

 public double getHigh() {
	return this.high;
 }

 
}
