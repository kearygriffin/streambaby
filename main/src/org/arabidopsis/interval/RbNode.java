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
public class RbNode {
    public double key;
    public boolean color;
    public RbNode parent;
    public RbNode left;
    public RbNode right;

    public static boolean BLACK = false;
    public static boolean RED = true;

    private RbNode() {
	// Default constructor is only meant to be used for the
	// construction of the NIL node.
    }

    public RbNode(double key) {
	this.parent = NIL;
	this.left = NIL;
	this.right = NIL;
	this.key = key;
	this.color = RED;
    }


    static RbNode NIL;
    static {
	NIL = new RbNode();
	NIL.color = BLACK;
	NIL.parent = NIL;
	NIL.left = NIL;
	NIL.right = NIL;
    }


    public boolean isNull() {
	return this == NIL;
    }


    public String toString() {
	if (this == NIL) { return "nil"; }
	return 
	    "(" + this.key + " " + (this.color == RED ? "RED" : "BLACK") +
	    " (" + this.left.toString() + ", " + this.right.toString() + ")";
    }
}
