package com.unwiredappeal.virtmem;
//==============================================================================
//Author:          Franco Pitta
//Date:            2001-05-10
//Developed with:  JBuilder 4.0, JDK 1.3.0
//==============================================================================

import com.unwiredappeal.virtmem.Memory;
import com.unwiredappeal.virtmem.MemoryArea;

/**
* This class works as a template for all implementations
* of allocation methods.
* It contains detailed information about the implemented allocation method
* like a short- and a long-name.
*/
public abstract class AllocationMethod {
/**
* Short name of the allocation method.
*/
private String shortName = "";

/**
* Long name of the allocation method.
*/
private String longName  = "";

/**
* Creates a new instance of this allocation method.
*/
public AllocationMethod(String sName, String lName) {
 super();
 this.shortName            = sName;
 this.longName             = lName;
}

/**
* Returns the short name of this allocation method.
*/
public String getShortName() {
 return this.shortName;
}

/**
* Returns the long name of this allocation method.
*/
public String getLongName() {
 return this.longName;
}

/**
* Returns the number of free memory areas.
*/
public int getFreeAreasCount(Memory freeMemory) {
 return freeMemory.getMemoryAreaCount();
}

/**
* Returns the usage of memory as a percentage.
* Usage = ( usedMemory * 100 ) / TotalMemory
*/
public int getUsage(Memory usedMemory) {
 return  (int) ((usedMemory.getMemoryAreaTotaSize())  * 100
             /  usedMemory.getMemorySize());
}

/**
* Returns the internal fragmentation of memory as a percentage.
* Internal fragmentation: ( WastedMemory / TotalMemory ) * 100
*     WastedMemory = allocated, but unused memory
*/
public int getIntFrag(Memory usedMemory) {
 if (usedMemory.getMemoryAreaTotaSize() == 0)
   // If no memory areas are used, there is no internal fragmentation.
   return 0;
 else {
   return  (int) ((usedMemory.getMemoryAreaTotaSize() -
                   usedMemory.getMemoryAreaTotalUsedSize()) * 100
                 / usedMemory.getMemoryAreaTotaSize());
 }
}

/**
* Returns true, if the full free memory area should be used.
* This can be used (e.g. used in "Buddy" and "Manual"for handling the
* free memory almost fully in the individual allocation method.
* This can stop the programm from cutting the returned free memory area
* when it is forbidden: In Buddy, the used size is often different
* from the allocated size of the memory area.
* When this method returns false, it is allowed to cut off the beginning or
* the end of the free memory area (FirstFit, BestFit, ..).
*/
public boolean isUseFullFreeMemoryArea() {
 return false;
}

/**
* Appends a new memory area into the memory structure
*/
@SuppressWarnings("unchecked")
public void appendMemoryArea(Memory usedMemory, MemoryArea newMemoryArea) {
 usedMemory.memoryVector.addElement(newMemoryArea);
}

/**
* Merge the given free memoryArea with the given free memory.
*/
public void mergeMemoryArea(Memory freeMemory, MemoryArea memoryArea) {
 freeMemory.mergeFreeMemoryArea(memoryArea);
}


/**
* Removes the requested memoryArea in the given memory
*/
public void removeMemoryArea(Memory memory, int startPos) {
 memory.removeMemoryArea(startPos);
}

/**
* Creates a new empty used memory.
*/
public Memory createUsedMemory(int newMemoryAreaCount, int newMemorySize) {
 return new Memory(newMemoryAreaCount, newMemorySize);
}

/**
* Creates a new random used memory.
*/
public Memory createRandomUsedMemory(Memory usedMemory) {
 return usedMemory.createRandomMemory();
}

/**
* Build up a free memory according to the given used memory.
*/
public Memory buildFreeMemory(Memory usedMemory) {
 return null;
}

/**
* Sorts the free memory structure to the needs of the
* implemented allocation method.
*/
public void sortFreeMemory(Memory memory) {
}

/**
* Sorts the used memory structure to the needs of the
* implemented allocation method.
*/
public void sortUsedMemory(Memory memory) {
}

/**
* Returns the free memory area that has been selected by the implemented
* allocation method. If none was found, return null.
*/
public MemoryArea getFreeArea(Memory freeMemory, int reqSize) {
 return null;
}
}