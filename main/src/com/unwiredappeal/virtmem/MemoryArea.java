package com.unwiredappeal.virtmem;
//==============================================================================
//Author:          Franco Pitta
//Date:            2001-04-23
//Developed with:  JBuilder 4.0, JDK 1.3.0
//==============================================================================

/**
* This class simulates a memory area in the memory structure and provides
* methods to handle the memory area.
*/
public class MemoryArea {
/**
* Starting position of the area where it is located.
*/
private int startPos;

/**
* Size of this memory area in MB.
*/
private int size;

/**
* Really Used size of this memory area in MB.
* Different from the allocated e.g. in Buddy-System. (Internal Fragmentation)
*/
private int usedSize;


/**
* Initial value for the starting position of a new memory area.
*/
public final static int INIT_STARTPOS = -1;

/**
* Initial value for the size of a new memory area.
*/
public final static int INIT_SIZE = 0;

/**
* Initial value for the processNr of a new memory area.
*/
public final static int INIT_PROCESSNR = 0;

/**
* Creates a new memory area with initial values.
*/
public MemoryArea() {
 super();
 this.setStartPos(INIT_STARTPOS);
 this.setSize(INIT_SIZE);
 this.setUsedSize(INIT_SIZE);
}


/**
* Creates a new memory area with given values.
*/
public MemoryArea(int newStartPos, int newSize) {
 this();
 this.setStartPos(newStartPos);
 this.setSize(newSize);
 this.setUsedSize(newSize);
}

/**
* Creates a new memory area with given values.
*/
public MemoryArea(int newStartPos, int newSize, int usedSize) {
 this();
 this.setStartPos(newStartPos);
 this.setSize(newSize);
 this.setUsedSize(usedSize);
}


/**
 * Returns a deep copy of the memory area, all elements are new created.
 */
public MemoryArea deepCopy(){
  MemoryArea newMemoryArea = new MemoryArea(this.getStartPos(),
                                            this.getSize());
  newMemoryArea.setUsedSize(this.getUsedSize());
  //newMemoryArea.setProcessNumber(this.getProcessNumber());
  return newMemoryArea;
}


/**
* Returns the startting position of this memory area.
*/
public int getStartPos() {
 return this.startPos;
}

/**
* Returns the size of this memory area.
*/
public int getSize() {
 return this.size;
}

/**
* Returns the usedSize of this memory area.
*/
public int getUsedSize() {
 return this.usedSize;
}



/**
* Returns true, if values of this memory area are initial.
*/
public boolean isInitial() {
 return (this.getSize()      == INIT_SIZE &&
         this.getStartPos()  == INIT_STARTPOS);
}

/**
* Sets the staringPos of this memory area in MB.
*/
public void setStartPos(int newStartPosKB) {
 this.startPos = newStartPosKB;
}


/**
* Sets the size of this memory area.
*/
public void setSize(int newSizeKB) {
 this.size = newSizeKB;
}

/**
* Sets the usedSize of this memory area.
*/
public void setUsedSize(int newSizeKB) {
 this.usedSize = newSizeKB;
}
}