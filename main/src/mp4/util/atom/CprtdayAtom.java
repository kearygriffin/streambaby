package mp4.util.atom;

public class CprtdayAtom extends AppleMetaAtom {

	public CprtdayAtom(CprtdayAtom old) {
		super(old);
	}

	public CprtdayAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'd', 'a', 'y' } );
	}
	
	public String getYear() {
		return new String(getMetaData());
	}

}
