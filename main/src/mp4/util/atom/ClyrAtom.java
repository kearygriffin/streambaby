package mp4.util.atom;

public class ClyrAtom extends AppleMetaAtom {

	public ClyrAtom(ClyrAtom old) {
		super(old);
	}

	public ClyrAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'l', 'y', 'r' } );
	}
	
	public String getLyrics() {
		return getStringMetadata();
	}

}
