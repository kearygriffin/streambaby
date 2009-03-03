/**
 * 
 */
package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class ContainerAtom extends Atom {
  
  /**
   * Create a container atom
   * @param type the atom's type information
   */
	
  protected List<Atom> unkChildren = new ArrayList<Atom>();
  protected ContainerAtom(byte[] type) {
    super(type);
  }
  
  /**
   * Copy constructor
   * @param old the version to copy
   */
  protected ContainerAtom(ContainerAtom old) {
    super(old);
    for (Atom a: old.unkChildren) {
    	// Should probably be a.copy(), but let's try this for time being(?)
    	unkChildren.add(a);
    }
  }
  
  public boolean isContainer() {
    return true;
  }
  
  public void addChild(Atom child) {
	  unkChildren.add(child);
  }
  
  /**
   * Recompute the size of the container by summing the size of each
   * contained atom
   */
  protected void recomputeSize() {
	  long newSize = unknownChildrenSize();
	  setSize(ATOM_HEADER_SIZE + newSize);
  }
  
  protected void addUnknownChild(Atom child) {
	  unkChildren.add(child);
  }
  
  protected void writeUnknownChildren(DataOutput out) throws IOException {
	  for (Atom a : unkChildren) {
		  a.writeData(out);
	  }
  }
  protected long unknownChildrenSize() {
	  long siz = 0;
	  for (Atom a : unkChildren)
		  siz += a.size();
	  return siz;
  }
  
  @Override
  public void writeData(DataOutput out) throws IOException {
    writeHeader(out);
    writeUnknownChildren(out);
  }
}