package mp4.util.atom;

public class CprtnamAtom extends AppleMetaAtom {

	public CprtnamAtom(CprtnamAtom old) {
		super(old);
	}

	public CprtnamAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'n', 'a', 'm' } );
	}
	public String getTitle() {
		return getStringMetadata();
	}

}
