package mp4.util.atom;

public class CwrtAtom extends AppleMetaAtom {

	public CwrtAtom(CwrtAtom old) {
		super(old);
	}

	public CwrtAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'w', 'r', 't' } );
		
		}
	public String getComposer() {
		return getStringMetadata();
	}
}
