package mp4.util.atom;

public class CgenAtom extends AppleMetaAtom {

	public CgenAtom(CgenAtom old) {
		super(old);
	}

	public CgenAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'g', 'e', 'n' } );
		
		}
	public String getGenre() {
		return getStringMetadata();

	}
}
