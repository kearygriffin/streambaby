package mp4.util.atom;

public class CprtlyrAtom extends AppleMetaAtom {

	public CprtlyrAtom(CprtlyrAtom old) {
		super(old);
	}

	public CprtlyrAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'l', 'y', 'r' } );
	}
	
	public String getLyrics() {
		return getStringMetadata();
	}

}
