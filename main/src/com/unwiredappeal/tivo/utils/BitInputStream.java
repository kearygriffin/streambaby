package com.unwiredappeal.tivo.utils;
import java.io.*;

public class BitInputStream
{
  private InputStream in;
  private int buffer;
  private int nextBit = 8;
  private int markBuffer;
  private int markNextBit;
  boolean markSupported = false;
  boolean reverse = false;
  
  public BitInputStream( InputStream in, boolean reverse) {
    this.in = in;
    markSupported = in.markSupported();
    this.reverse = reverse;
  }
  public BitInputStream( InputStream in) {
	  this(in, false);
  }
  protected void resetBitPositions() {
	  nextBit = 8;
  }
  public int readBit() throws IOException {
    if (in == null)
      throw new IOException( "Already closed" );

    if (nextBit==8) {
      buffer = in.read();

      if (buffer==-1)
        throw new EOFException();

      nextBit = 0;
    }

    int nbActual = nextBit;
    if (reverse)
    	nbActual = 7 - nextBit;
    int bit = buffer & (1<<nbActual);
    nextBit++;

    bit = bit == 0 ? 0 : 1;

    return bit;
  }

  public void close() throws IOException {
    in.close();
    in = null;
  }
  
  public boolean markSupported() {
	  return markSupported;
  }
  
  public void mark(int readLimit) {
	  in.mark((readLimit+7)/8);
	  markBuffer = buffer;
	  markNextBit = nextBit;
  }
  
  public void reset() throws IOException {
	  in.reset();
	  buffer = markBuffer;
	  nextBit = markNextBit;
  }
  
  public long skip(long n) throws IOException {
	  long cbits = n;
	  long ret = 0;
	  while(nextBit != 8 && cbits > 0) {
		  try {
			  readBit();
		  } catch(EOFException e) {
			  return ret;
		  }
		  cbits--;
		  ret++;
	  }
	  long skipBytes = cbits % 8;
	  long skippedBytes = in.skip(skipBytes);
	  ret += (8*skippedBytes);
	  cbits -= (8*skippedBytes);
	  if (skipBytes != skippedBytes) {
		  return ret;
	  }
	  
	  while(cbits > 0) {
		  try {
			  readBit();
		  } catch(EOFException e) {
			  return ret;
		  }
		  cbits--;
		  ret++;		  
	  }
	  
	  return ret;
	  
  }
  
}
