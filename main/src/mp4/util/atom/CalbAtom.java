package mp4.util.atom;

public class CalbAtom extends AppleMetaAtom {

	public CalbAtom(CalbAtom old) {
		super(old);
	}

	public CalbAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'a', 'l', 'b' } );
	}
	
	public String getAlbum() {
		return getStringMetadata();
	}

}
