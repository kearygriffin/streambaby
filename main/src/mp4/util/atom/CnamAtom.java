package mp4.util.atom;

public class CnamAtom extends AppleMetaAtom {

	public CnamAtom(CnamAtom old) {
		super(old);
	}

	public CnamAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'n', 'a', 'm' } );
	}
	public String getTitle() {
		return getStringMetadata();
	}

}
