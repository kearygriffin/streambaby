/**
 * 
 */
package mp4.util.atom;

/**
 * The user data atom.
 */
public class IlstAtom extends ContainerAtom {

  /**
   * Constructor for a user-data atom.
   */
  public IlstAtom() {
    super(new byte[]{'i','l','s','t'});
  }
  
  /**
   * Copy constructor.  Perform deep copy.
   * @param old the version to copy
   */
  public IlstAtom(IlstAtom old) {
    super(old);
  }
  


  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
}