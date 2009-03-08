package mp4.util.atom;

public class CprtartAtom extends AppleMetaAtom {

	public CprtartAtom(CprtartAtom old) {
		super(old);
	}

	public CprtartAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'a', 'r', 't' } );
	}
	
	public String getArtist() {
		return getStringMetadata();
	}

}
