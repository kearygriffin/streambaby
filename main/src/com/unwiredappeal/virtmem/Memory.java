//==============================================================================
// Author:          Franco Pitta
// Date:            2001-04-23
// Developed with:  JBuilder 4.0, JDK 1.3.0
//==============================================================================

package com.unwiredappeal.virtmem;

import java.util.*;
/**
 * This class simulates the memory structure and provides methods
 * to handle the memory areas in this memory structure.
 * The number of areas isn't fix and can be changed at runtime.
 * To set up the maximum memory size (512 MB) the constants for the
 * memory size have to be added and the number of memory vectors has to be
 * increased.
 */
@SuppressWarnings("unchecked")
public class Memory {

  /**
	 * 
	 */
	private static final long serialVersionUID = 8982540786722597726L;

  Vector memoryVector = new Vector();
  /**
   * Actual size of Memory as an int-value.
   */
  private int memorySize;

  /**
   * Vectors for the memory management in the allocation method "buddy system".
   * Each vector contains the memory areas with a special size.
   * The sizes are a power of two: 2^0, 2^1, 2^2, ... 2^9 for example.
   * These entries are used for the buddy system.
   * They are also added to the flat structur of this class, the "main vector".
   * This is done to have a similar place where all memory areas can be accessed
   */
  private Memory memorySizeVector[] = new Memory[32];
  /*{null, null, null, null, null,
                                        null, null, null, null, null}; */

  /**
   * Create a new instance of this Memory.
   */
  public Memory() {
    super();
    this.memorySize         = 0;
  }

  /**
   * Create a new instance of Memory with given number of memory areas and
   * the given memorySize.
   */
  public Memory(int newMemoryAreaCount, int newMemorySize) {
    this();
    for (int i=0; i < newMemoryAreaCount; i++)
      memoryVector.addElement(new MemoryArea());

    this.setMemorySize(newMemorySize);
  }

  public void printMemory() {
    System.out.println("+++ Start printMemory" +
                        "\nindex Process, StartPos, UsedSize, Size, Color");
    if (this.getMemoryAreaCount() == 0)
      System.out.println("\n empty");
    else
      for (int i=0; i<this.getMemoryAreaCount(); i++)
        System.out.println(i                                          + " "
                           + this.getMemoryArea(i).getStartPos()      + " "
                           + this.getMemoryArea(i).getUsedSize()      + " "
                           + this.getMemoryArea(i).getSize()          + " "
                           );
    System.out.println("\n+++ End printMemory");
  }

  public void printMemoryBuddy() {
    MemoryArea ma;

    System.out.println("+++ Start printMemoryBuddy" +
                        "\n Process, StartPos, UsedSize, Size, Color");
    for (int i=0; i < this.getMemorySizeVectorLength(); i++) {
      if (this.getMemorySizeVector(i) != null &&
          this.getMemorySizeVector(i).getMemoryAreaCount() > 0) {
        System.out.print("\n Loop "+ i);
        for (int j=0; j < this.getMemorySizeVector(i).getMemoryAreaCount(); j++) {
          ma = this.getMemorySizeVector(i).getMemoryArea(j);
          System.out.print(" - "
                                + ma.getStartPos()
                                + " "
                                + ma.getUsedSize()
                                + " "
                                + ma.getSize());
        }
      }
    }
    System.out.println("\n+++ End printMemoryBuddy");
  }

  /**
   * Create a new instance of Memory with random values.
   */
  public Memory createRandomMemory() {
    Memory      newMemory   = new Memory();
    MemoryArea  ma          = null;
    Random      random      = new Random();

    newMemory.setMemorySize(this.memorySize);

    // Create new memory areas in a loop and put them into the newMemory.
    for (int i=0; i < this.getMemoryAreaCount(); i++) {
      // Create a new memory area again and again until it fits into the
      // newMemory.
      do {
        // If the the new memory area does'nt fit into the newMemory,
        // reset decrease the number of used colors in newMemory and
        // create a new one.
        ma = new MemoryArea(random.nextInt(newMemory.memorySize),
                            random.nextInt(newMemory.memorySize
                                            / this.getMemoryAreaCount()));

        // Set the process number to where this memory area belongs
      } while (!(newMemory.fitInMemory(newMemory, ma)));
      newMemory.memoryVector.addElement(ma);
    }
    return newMemory;
  }

  /**
   * Builds a memory structure containing the free memory areas
   * depending on the given usedMemory.
   */
  public static Memory buildFreeMemory(Memory usedMemory) {
    Memory  freeMemory  = new Memory();
    int     index       = 0;

    // Is used memory empty? Then free memory = whole memory
    if (usedMemory.getMemoryAreaFilledCount() == 0)
        freeMemory.appendMemoryArea(new MemoryArea(0,
                                  usedMemory.memorySize));
    else {
      // Check free space before first memory area
      if (usedMemory.getMemoryArea(index).getStartPos() > 0)
        freeMemory.appendMemoryArea(new MemoryArea(0,
                                  usedMemory.getMemoryArea(index).getStartPos()));
      // Check free space after each used memory area except of the last one
      for (; index < usedMemory.getMemoryAreaCount()-1; index++){
        if (usedMemory.getMemoryArea(index).getStartPos()
              + usedMemory.getMemoryArea(index).getSize()
            < usedMemory.getMemoryArea(index+1).getStartPos())
          freeMemory.appendMemoryArea(new MemoryArea(
                              usedMemory.getMemoryArea(index).getStartPos()
                              + usedMemory.getMemoryArea(index).getSize(),
                              usedMemory.getMemoryArea(index+1).getStartPos()
                              - usedMemory.getMemoryArea(index).getStartPos()
                              - usedMemory.getMemoryArea(index).getSize()));
      }

      // Check free space after last used memory area
      index = usedMemory.getMemoryAreaCount()-1;
      if (usedMemory.getMemoryArea(index).getStartPos()
            + usedMemory.getMemoryArea(index).getSize()
          < usedMemory.getMemorySize())
          freeMemory.appendMemoryArea(new MemoryArea(
                                  usedMemory.getMemoryArea(index).getStartPos()
                                  + usedMemory.getMemoryArea(index).getSize(),
                                  usedMemory.getMemorySize()
                                  - usedMemory.getMemoryArea(index).getStartPos()
                                  - usedMemory.getMemoryArea(index).getSize()));
    }
    return freeMemory;
  }

  /**
   * Merges the given memory are with any other existing or adds it to the list.
   */
  public void mergeFreeMemoryArea(MemoryArea memoryArea) {
    MemoryArea  actMA     = null,
                mergeMA   = memoryArea;
    boolean     isMerged  = false,
                isNewLoop = false;  // Indicator, to start a new merge loop
    int         index     = 0;

    while (index < this.getMemoryAreaCount()) {
      actMA = this.getMemoryArea(index);
      if ((actMA.getStartPos() + actMA.getSize()) == mergeMA.getStartPos()) {
        // memoryArea is directly behind another free memory area
        // Case: ... | Free | Release | Alloc | ...       or
        // Case: ... | Free | Release | Free | ...        or
        // Case: ... | Free | Release |
        actMA.setSize(actMA.getSize() + mergeMA.getSize());
        this.removeMemoryArea(mergeMA.getStartPos());
        isMerged  = true;
        isNewLoop = true;
        mergeMA   = actMA;
      }
      if (!isNewLoop &&
          mergeMA.getStartPos() + mergeMA.getSize() == actMA.getStartPos()) {
        // memoryArea is directly in front of another free memory area
        // Case: ... | Alloc | Release | Free | ...       or
        // Case:             | Release | Free | ...
        mergeMA.setSize(mergeMA.getSize() + actMA.getSize());
        this.removeMemoryArea(actMA.getStartPos());
        if (!this.isInMemory(mergeMA.getStartPos()))
          this.appendMemoryArea(mergeMA);
        isMerged  = true;
        isNewLoop = true;
      }
      if (isNewLoop)
        // Start new merge loop, because 2 memory areas have been merged
        index = 0;
      else
        // compare with next memory area
        index++;

      // Reset indicator, after the loop index has been reseted.
      isNewLoop = false;
    }
    if (!isMerged)
      // the memoryArea cannot be merged with another one, append it
      // Case: ... | Alloc | Release | Alloc | ...
      this.appendMemoryArea(memoryArea);
  }

  /**
   * Appends a new memory area into the memory structure
   */
  public void appendMemoryArea(MemoryArea newMemoryArea) {
    memoryVector.addElement(newMemoryArea);
  }

  /**
   * Inserts a new memory area into the memory structure regarding the sort
   * starting position
   */
  public void insertMemoryArea(MemoryArea newMemoryArea) {
    int index = 0;

    while (index < this.getMemoryAreaCount() &&
          this.getMemoryArea(index).getStartPos() < newMemoryArea.getStartPos())
      index++;
    memoryVector.add(index, newMemoryArea);
  }

  /**
   * Returns true, if the memory has only initial memory areas.
   */
  public boolean isInitial() {
    boolean isInitial = true;

    for (int i=0; i < this.getMemoryAreaCount(); i++)
      if (!this.getMemoryArea(i).isInitial())
        isInitial = false;
    return isInitial;
  }

  /**
   * Returns true, if a memory area with the given starting position
   * is in memory.
   */
  public boolean isInMemory(int startingPos) {
    return (this.getMemoryAreaStart(startingPos) != null);
  }

  /**
   * Appends the given number of initial memory areas to the memory.
   */
  public void appendMemoryAreas(int deltaMemoryAreaCount) {
    for (int i=0; i < deltaMemoryAreaCount; i++)
      memoryVector.addElement(new MemoryArea());
  }

  /**
   * Removes all memory areas from memory.
   */
  public void removeAllMemoryAreas() {
    memoryVector.removeAllElements();
  }


  /**
   * Removes memory area from memory woth the given starting position.
   */
  public void removeMemoryArea(int startPos) {
    MemoryArea oldMemoryArea = this.getMemoryAreaStart(startPos);

    if (oldMemoryArea != null) {
      memoryVector.removeElement(oldMemoryArea);
    }
  }

  /**
   * Checks, if the given memory area does fit in this memory.
   * It does not overlap with the exisiting memory areas and the size
   * must be appropriate.
   */
  private boolean fitInMemory(Memory memory, MemoryArea  newMA) {
    boolean     fits  = true;
    MemoryArea  oldMA = null;

    // Is the statrting position and size valid?
    if (  // new memory area is greater than maximum memory
          newMA.getStartPos() + newMA.getSize() > memory.memorySize     ||
          // The size is too small
          newMA.getSize() < 1
        )
      fits = false;

    // Does the new memory area not overlap with any existing memory area?
    for (int i=0; i < memory.getMemoryAreaCount() && fits == true; i++) {
      oldMA = memory.getMemoryArea(i);
      if (  // beginning of new MemoryArea is in the existing memory area
            (oldMA.getStartPos() <= newMA.getStartPos() &&
              oldMA.getStartPos() + oldMA.getSize()
              >= newMA.getStartPos())                                    ||
            // end of new MemoryArea is in the existing memory area
            (oldMA.getStartPos()
              <= newMA.getStartPos() + newMA.getSize() &&
              oldMA.getStartPos() + oldMA.getSize()
              >= newMA.getStartPos() + newMA.getSize())                  ||
            // existing memory area fits in the new Memory area
            (oldMA.getStartPos() >= newMA.getStartPos() &&
              oldMA.getStartPos() + oldMA.getSize()
              <= newMA.getStartPos() + newMA.getSize())
          )
        fits = false;
    }
    return fits;
  }


  /**
   * Returns the memory in the memorySizeVector with the given index.
   * Only needed for buddy system.
   */
  public Memory getMemorySizeVector(int index) {
    return this.memorySizeVector[index];
  }

  /**
   * Returns the length of memorySizeVector.
   * Only needed for buddy system.
   */
  public int getMemorySizeVectorLength() {
    return this.memorySizeVector.length;
  }

  /**
   * Sets the given memory in the memorySizeVector at the given index.
   * Only needed for buddy system.
   */
  public void setMemorySizeVector(Memory newMemory, int index) {
    this.memorySizeVector[index] = newMemory;
  }

  /**
   * Returns the memory area at a special index.
   */
  public MemoryArea getMemoryArea(int index) {
    return (MemoryArea) ( (memoryVector.size() > 0)
                          ? memoryVector.elementAt(index)
                          : null);
  }

  /**
   * Returns the index of the given memory area.
   */
  public int getIndexOfMemoryArea(MemoryArea  memoryArea) {
    return memoryVector.indexOf(memoryArea);
  }

  /**
   * Returns the total size of all not initial memory areas.
   */
  public int getMemoryAreaTotalUsedSize() {
    int size = 0;

    for (int i=0; i < this.getMemoryAreaCount(); i++)
      if (!this.getMemoryArea(i).isInitial())
        size += this.getMemoryArea(i).getUsedSize();
    return size;
  }

  /**
   * Returns the total size of all not initial memory areas.
   */
  public int getMemoryAreaTotaSize() {
    int size = 0;

    for (int i=0; i < this.getMemoryAreaCount(); i++)
      if (!this.getMemoryArea(i).isInitial())
        size += this.getMemoryArea(i).getSize();
    return size;
  }

  /**
   * Returns the memory area with a special starting position.
   */
  public MemoryArea getMemoryAreaStart(int startPos) {
    MemoryArea  ma    = null;
    int         index = 0;

    while (ma == null && index < this.getMemoryAreaCount()) {
      if (this.getMemoryArea(index).getStartPos() == startPos)
        ma = this.getMemoryArea(index);
      index++;
    }
    return (MemoryArea) ma;
  }

  /**
   * Returns the memory area that contains the given memory position or
   * if none exists, return the first free memory area.
   */
  public MemoryArea getMemoryAreaPos(int memoryPos) {
    MemoryArea  ma    = null;
    int         index = 0;

    // Find memory area containt the given memory position
    while (ma == null && index < this.getMemoryAreaCount()) {
      if (this.getMemoryArea(index).getStartPos() <= memoryPos  &&
          this.getMemoryArea(index).getStartPos()
          + this.getMemoryArea(index).getSize()-1 >= memoryPos)
        ma = this.getMemoryArea(index);
      index++;
    }

    // If no memory area was found, return the first free memory area, if exists
    if (ma == null && this.getMemoryAreaCount() > 0)
      ma = this.getMemoryArea(0);
    return (MemoryArea) ma;
  }

  /**
   * Returns the number of memory areas in this memory.
   */
  public int getMemoryAreaCount() {
    return memoryVector.size();
  }

  /**
   * Returns the number of not initial memory areas in this memory.
   */
  public int getMemoryAreaFilledCount() {
    int count=0;
    for (int j=0; j<this.getMemoryAreaCount(); j++)
      if (!this.getMemoryArea(j).isInitial())
        count++;
    return count;
  }

  /**
   * Returns the size of this memory.
   */
  public int getMemorySize() {
    return this.memorySize;
  }

  /**
   * Sets the size of this memory.
   */
  public void setMemorySize(int newMemorySize) {
    this.memorySize = newMemorySize;
  }


  /**
   * Sorts the memory allocations descending by the size.
   */
  public void sortDescSize() {
    MemoryArea  actualMA;
    int         i,
                j;

    for (i=1; i < this.getMemoryAreaCount(); i++) {
      actualMA = this.getMemoryArea(i);
      j = 0;
      while (this.getMemoryArea(j).getSize() > actualMA.getSize() &&
              j <= i)
        j++;
      memoryVector.removeElement(actualMA);
      memoryVector.add(j, actualMA);
    }
  }

  /**
   * Sorts the memory allocations ascending by the size.
   */
  public void sortAscSize() {
    MemoryArea  actualMA;
    int         i,
                j;

    for (i=1; i < this.getMemoryAreaCount(); i++) {
      actualMA = this.getMemoryArea(i);
      j = 0;
      while (this.getMemoryArea(j).getSize() < actualMA.getSize() &&
              j <= i)
        j++;
      memoryVector.removeElement(actualMA);
      memoryVector.add(j, actualMA);
    }
  }


  /**
   * Sorts the memory allocations ascending by the starting position.
   */
  public void sortAscStart() {
    MemoryArea  actualMA;
    int         i,
                j;

    for (i=1; i < this.getMemoryAreaCount(); i++) {
      actualMA = this.getMemoryArea(i);
      j = 0;
      while (this.getMemoryArea(j).getStartPos() < actualMA.getStartPos() &&
              j <= i)
        j++;
      memoryVector.removeElement(actualMA);
      memoryVector.add(j, actualMA);
    }
  }
}