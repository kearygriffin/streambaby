package com.unwiredappeal.virtmem;

public abstract class AbstractMemoryManager {
	Memory freeMemory = new Memory();
	AllocationMethod allocMethod = new Buddy();
	
	public synchronized MemChunk alloc(int size) {
		MemoryArea area = null;
		boolean outOfMemory = false;
		do {
			area = allocMethod.getFreeArea(freeMemory, size);
			if (area == null) {
				MemoryArea newMemArea = requestMoreMemory(size);
				if (newMemArea != null)
					allocMethod.mergeMemoryArea(freeMemory, newMemArea);
				else
					outOfMemory = true;
			}
			
		} while(area == null && !outOfMemory);
		if (area == null)
			return null;
		else
			return createMemChunk(area);
	}
	
	public synchronized void free(MemChunk chunk) {
		chunk.free();
	}
	
	public abstract MemoryArea requestMoreMemory(int size);
	public abstract MemChunk createMemChunk(MemoryArea area);
	
}
