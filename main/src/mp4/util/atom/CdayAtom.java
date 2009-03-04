package mp4.util.atom;

public class CdayAtom extends AppleMetaAtom {

	public CdayAtom(CdayAtom old) {
		super(old);
	}

	public CdayAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'd', 'a', 'y' } );
	}
	
	public String getYear() {
		return new String(getMetaData());
	}

}
