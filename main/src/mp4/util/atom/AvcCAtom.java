package mp4.util.atom;

public class AvcCAtom extends LeafAtom {


	  /**
	   * Constructor that creates an empty avcc atom.
	   */
	  public AvcCAtom() {
	    super(new byte[]{'a','v','c','C'});
	  }
	  
	  /**
	   * Copy constructor.  Performs a deep copy
	   * @param old the version to copy
	   */
	  public AvcCAtom(AvcCAtom old) {
	    super(old);
	  }

	@Override
	public void accept(AtomVisitor v) throws AtomException {
		v.visit(this);
	}
	

}
