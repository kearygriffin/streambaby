package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;

public class Avc1Atom extends HybridAtom {

	  public static final int DATA_SIZE = 78;

	  private static final int WIDTH_OFFSET = 24;
	  private static final int HEIGHT_OFFSET = 26;
	  private AvcCAtom avcc;
	  
	  /**
	   * Constructor that creates an empty avc1 atom.
	   */
	  public Avc1Atom() {
	    super(new byte[]{'a','v','c','1'});
	  }
	  
	  /**
	   * Copy constructor.  Performs a deep copy
	   * @param old the version to copy
	   */
	  public Avc1Atom(Avc1Atom old) {
	    super(old);
	    if (old.avcc != null)
	    	this.avcc = new AvcCAtom(old.avcc);
	  }

	@Override
	public void accept(AtomVisitor v) throws AtomException {
		v.visit(this);
	}
	
	public int getWidth() {
		return data.getUnsignedShort(WIDTH_OFFSET);
	}

	public int getHeight() {
		return data.getUnsignedShort(HEIGHT_OFFSET);
	}
	
	  
	public long pureDataSize() {
		  return DATA_SIZE;
	}

	@Override
	public void addChild(Atom child) {
		if (child instanceof AvcCAtom)
			  avcc = (AvcCAtom)child;
		  else 
			  addUnknownChild(child);
	}  
	  
	public AvcCAtom getAvcc() {
		  return avcc;
	}
	  
	 /**
	   * Write the avc1 atom data to the specified output
	   * @param out where the data goes
	   * @throws IOException if there is an error writing the data
	   */
	  @Override
	  public void writeData(DataOutput out) throws IOException {
	    writeHeader(out);
	    data.writeData(out);
	    if (avcc != null)
	    	avcc.writeData(out);
	    writeUnknownChildren(out);
	  }
}
