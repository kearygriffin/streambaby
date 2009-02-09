package com.unwiredappeal.virtmem;
//==============================================================================
//Author:          Franco Pitta
//Date:            2001-05-30
//Developed with:  JBuilder 4.0, JDK 1.3.0
//==============================================================================

import java.util.Random;

import com.unwiredappeal.virtmem.Memory;
import com.unwiredappeal.virtmem.MemoryArea;

/**
* Implementation of the allocation method Buddy-System.
* The selected memory area for allocation is the first free memory area with
* sufficient size. The search for this memory area starts the first time
* at the first free memory area.
* Every succeding search continues at the last resulting free memory area
* if one exist, else at the next free memory area following the allocated one.
* The free memory areas are sorted ascending by the starting
* position in memory.
*/
public class Buddy extends AllocationMethod {

/**
* Long name of this allocation method
*/
public final static String SHORTNAME = "Buddy";

/**
* Short name of this allocation method
*/
public final static String LONGNAME = "Buddy-System";

public static int DEFAULT_MIN_MEM_BLOCK = 4096;
public int minMemBlock = DEFAULT_MIN_MEM_BLOCK;
/**
* Buddy-System is an implementation of the allocatio method class
* and searches for the next avalable free memory area starting from
* the beginning of memory and resuming the search each time at the last
* stop point.
*/
public Buddy() {
 super(SHORTNAME, LONGNAME);
}

public Buddy(int minMemBlock) {
	 this();
	 this.minMemBlock = minMemBlock;
	}

public void setMinMemBlock(int minMemBlock) {
	this.minMemBlock = minMemBlock;
}

/**
* Creates a new empty used memory.
*/
public Memory createUsedMemory(int newMemoryAreaCount, int newMemorySize) {
 return new Memory(0, newMemorySize);
}

/**
* Creates a new random used memory regarding the specific needs
* to the memory structure of the buddy system.
*/
public Memory createRandomUsedMemory(Memory usedMemory) {
 Memory      newMem,
             freeMem;
 // newUsedMA,
 MemoryArea              oldFreeMA,
             newFreeMA   = null;
 Random      random      = new Random();
 int         randomRequest;

 // Create a new used memory, that will be filled in the later for-loop
 // and overtake the memory size without any change.
 newMem  = new Memory();
 newMem.setMemorySize(usedMemory.getMemorySize());

 // Create a free memory with one big free memory area.
 freeMem = this.buildFreeMemory(usedMemory);

 // Create new memory areas in a loop and put them into the new used memory.
 for (int i=0; i < usedMemory.getMemoryAreaCount(); i++) {
   randomRequest = random.nextInt(usedMemory.getMemorySize()
                                   / usedMemory.getMemoryAreaCount());

   // Find a fitting free memory area. Add it to the new used memory
   // and put the rest of the memory area to the list of free memory area.
   oldFreeMA = this.getFreeArea(freeMem, randomRequest);
   if (oldFreeMA != null) {
     newFreeMA = oldFreeMA;
     freeMem.removeMemoryArea(oldFreeMA.getStartPos());
     newFreeMA.setUsedSize(randomRequest);
     newFreeMA.setProcessNumber(i+1);
     this.appendMemoryArea(newMem, newFreeMA);
   }
 }

 return newMem;
}

/**
* Build up a free memory according to the given used memory.
*/
public Memory buildFreeMemory(Memory usedMemory) {
 Memory      freeMemory      = new Memory(0, 0),
             tempUsedMemory  = new Memory(0, usedMemory.getMemorySize()),
             tempUsedMemory2 = new Memory(0, 0);
 MemoryArea  freeMA;
 int         size;

 // Create a temporary help memory with all memory areas sorted by startPos
 // to use later on the method to build the free memory.
 tempUsedMemory = buildFlatMemory(usedMemory);
 tempUsedMemory.sortAscProcess();

 // Build upon the "tempUsedMemory" the free memory by adding the
 // memory areas to a temporary memory and getting as a step by step
 // result the new free memory.
 freeMemory = new Memory(0, usedMemory.getMemorySize());
 this.appendMemoryArea(freeMemory,
                       new MemoryArea(0, usedMemory.getMemorySize()));
 for (int i=1; i <= tempUsedMemory.getMemoryAreaCount(); i++) {
   // Find a fitting free memory area. Add it to the temporary memory
   // and put the rest of the memory area to the list of free memory area.
   size = tempUsedMemory.getMemoryArea(i-1).getSize();
   freeMA = this.getFreeArea(freeMemory, size);

   freeMemory.removeMemoryArea(freeMA.getStartPos());
   freeMA.setUsedSize(size);
   freeMA.setProcessNumber(i);
   this.appendMemoryArea(tempUsedMemory2, freeMA);
 }

 return freeMemory;
}

/**
* Build up a flat memory. Collect all memoryAreas in the memoryVector and
* append them to the "main Vector".
*/
public static Memory buildFlatMemory(Memory usedMemory) {
 Memory      tempUsedMemory = new Memory(0, usedMemory.getMemorySize()),
             actMemory;
 int         maxMemIndex = usedMemory.getMemorySizeVectorLength(),
             actMemIndex = 0,
             maCount;

 // Create a temporary help memory with all memory areas sorted by startPos.
 while(actMemIndex < maxMemIndex) {
   // Get the actual element of memory size vector and append
   // all memory areas to the help memory
   actMemory = usedMemory.getMemorySizeVector(actMemIndex);
   if (actMemory != null) {
     maCount = actMemory.getMemoryAreaCount();
     for (int actMA=0; actMA < maCount; actMA++)
       tempUsedMemory.appendMemoryArea(actMemory.getMemoryArea(actMA));
   }

   // Examine next element of memory size vector
   actMemIndex++;
 }
 tempUsedMemory.sortAscStart();

 return tempUsedMemory;
}

/**
* Appends a new memory area into the memory structure.
* The new memoryArea is appended to the memory in the vector with
* the
*/
public void appendMemoryArea(Memory usedMemory, MemoryArea newMemoryArea) {
 Memory tempMemory;

 // Append it to the normal, flat memory structure.
 super.appendMemoryArea(usedMemory, newMemoryArea);

 // Append it to the memory vector with the correct size
 if (usedMemory.getMemorySizeVector(
                             getLog(newMemoryArea.getSize())) != null)
   usedMemory.getMemorySizeVector(getLog(newMemoryArea.getSize()))
                                           .appendMemoryArea(newMemoryArea);
 else {
   tempMemory = new Memory(0, 0);
   tempMemory.appendMemoryArea(newMemoryArea);
   usedMemory.setMemorySizeVector(tempMemory,
                                   getLog(newMemoryArea.getSize()));
 }
}

/**
* Returns the number of free memory areas.
*/
public int getFreeAreasCount(Memory freeMemory) {
 return (buildFlatMemory(freeMemory)).getMemoryAreaCount();
}

/**
* Returns the first free memory area where the requestedSize fits into.
* Cases:
*        1. None was found, return null
*        2. A free memoryArea with exact size was found, return the
*            free memory area and remove it from the free memory
*        3. A free memoryArea with bigger size was foung, divide the big area
*            into 2 half-sized memory areas until the exact of the request
*            is found.
*            Return the memory area with the exact size and removeit from
*            the free memory.
*/
public MemoryArea getFreeArea(Memory freeMemory, int reqSize) {
 int         reqSizeRound,
             actMemIndex;
 Memory      tempMemory;
 MemoryArea  freeArea,
             tempMA,
             tempMA2;

 if (reqSize < minMemBlock)
	 reqSize = minMemBlock;

 // Round up the requestedSize to the next power of 2
 reqSizeRound  = getNextPow(reqSize);

 // Set the starting index to the memory with memoryAreasSize = reqSizeRound
 actMemIndex = getLog(reqSizeRound);

 // Find the first fitting memory area
 while (actMemIndex < freeMemory.getMemorySizeVectorLength()   &&
         ( freeMemory.getMemorySizeVector(actMemIndex) == null   ||
           freeMemory.getMemorySizeVector(actMemIndex)
                                             .getMemoryAreaCount() == 0))
   actMemIndex++;

 // If a fitting memory area was found, then continue...
 if (actMemIndex < freeMemory.getMemorySizeVectorLength()) {
   // Get the first fitting memory area
   tempMA = freeMemory.getMemorySizeVector(actMemIndex).getMemoryArea(0);

   // If the size is to big, split the free temp memory area to get
   // the exact size.
   if (tempMA.getSize() == reqSizeRound)
     freeMemory.getMemorySizeVector(actMemIndex).removeMemoryArea(
                                                     tempMA.getStartPos());
   else
     while (tempMA.getSize() > reqSizeRound) {
       // Remove the old free memory area
       freeMemory.getMemorySizeVector(actMemIndex).removeMemoryArea(
                                               tempMA.getStartPos());

       // Split the memory Area into two halves
       tempMA.setSize(tempMA.getSize()/2);
       tempMA2 = tempMA.deepCopy();
       tempMA2.setStartPos(tempMA2.getStartPos() + tempMA2.getSize());
       actMemIndex--;
       if (freeMemory.getMemorySizeVector(actMemIndex) != null)
         freeMemory.getMemorySizeVector(actMemIndex).appendMemoryArea(tempMA2);
       else {
         tempMemory = new Memory(0, 0);
         tempMemory.appendMemoryArea(tempMA2);
         freeMemory.setMemorySizeVector(tempMemory, actMemIndex);
       }
     }

   // The fitting free memory area was found
   freeArea = tempMA;
 } else
   freeArea = null;

 return freeArea;
}

/**
* Returns true, if the full free memory area should be used.
*/
public boolean isUseFullFreeMemoryArea() {
 return true;
}

/**
* Returns the corresponding buddy of the given memoryArea.
*/
private MemoryArea getBuddy(MemoryArea memoryArea) {
 MemoryArea ma         = new MemoryArea();
 int startPos          = memoryArea.getStartPos(),
     size              = memoryArea.getSize();
 boolean isLeftBuddy  = true;

 // Count how often the size fits into the starting position of the
 // given memory Area.
 // If   even: the left buddy is given, return the right buddy
 //    uneven: the right buddy is given, return the left buddy

 while (startPos >= size) {
   startPos      -= size;
   isLeftBuddy  = !isLeftBuddy;
 }

 ma.setSize(memoryArea.getSize());
 if (isLeftBuddy)
   ma.setStartPos(memoryArea.getStartPos() + memoryArea.getSize());
 else
   ma.setStartPos(memoryArea.getStartPos() - memoryArea.getSize());

 return ma;
}

/**
* Merge the given free memoryArea with the given free memory.
*/
public void mergeMemoryArea(Memory freeMemory, MemoryArea mergeMemoryArea) {
 Memory      memory;
 MemoryArea  mergeArea,
             buddyMA;
 int         index;
 boolean     isActualMerged;

 mergeArea = mergeMemoryArea;

 do {
   isActualMerged = false;
   // Get the buddy of the memory area to be merged
   buddyMA = this.getBuddy(mergeArea);

   // If a merge is possible, do it, else append memory area
   index = getLog(mergeArea.getSize());
   if ((memory = freeMemory.getMemorySizeVector(index)) != null) {
     // Check all memoryAreas to be equal to the searched one
     for (int i=0; i < memory.getMemoryAreaCount() &&
                   !isActualMerged; i++) {
       if (memory.getMemoryArea(i).getStartPos() == buddyMA.getStartPos()  &&
           memory.getMemoryArea(i).getSize()     == buddyMA.getSize()) {
         // Search successfull: Buddy is in free memory, merge it.
         buddyMA.setStartPos(  (mergeArea.getStartPos() <
                                 memory.getMemoryArea(i).getStartPos()
                                 ? mergeArea.getStartPos()
                                 : memory.getMemoryArea(i).getStartPos())
                             );
         buddyMA.setSize(buddyMA.getSize() * 2);

         // Remove old memory area in Vector and (!) in flat structure
         freeMemory.removeMemoryArea(memory.getMemoryArea(i).getStartPos());
         memory.removeMemoryArea(memory.getMemoryArea(i).getStartPos());

         // Append the merged one and set marker
         mergeArea = buddyMA;
         isActualMerged  = true;
       }
     }
   }
 } while (isActualMerged);

 // Append memory area
 index = getLog(mergeArea.getSize());
 if (freeMemory.getMemorySizeVector(index) == null)
   // List of memory areas with this size is null, create a list
   freeMemory.setMemorySizeVector(new Memory( 0, 0), index);
 freeMemory.getMemorySizeVector(index).appendMemoryArea(mergeArea);
}

/**
* Removes the requested memoryArea in the given memory
*/
public void removeMemoryArea(Memory memory, int startPos) {
 boolean isRemoved = false;

 super.removeMemoryArea(memory, startPos);

 for (int i=0; i < memory.getMemorySizeVectorLength()  && !isRemoved; i++)
   if (memory.getMemorySizeVector(i) != null       &&
       memory.getMemorySizeVector(i).isInMemory(startPos)) {
     memory.getMemorySizeVector(i).removeMemoryArea(startPos);
     isRemoved = true;
   }
}

public static int getLog(int intValue) {
	 int log = 0;

	 for (int i=intValue; i>1; i = i/2)
	   log++;

	 return log;
	}

	/**
	* Returns the next power of 2 that is <= the given integer value.
	*/
	public static int getNextPow(int intValue) {
	 int nextPower = 1;

	 while (nextPower < intValue)
	   nextPower *= 2;

	 return nextPower;
	}
}