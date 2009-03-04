package mp4.util.atom;

public class CartAtom extends AppleMetaAtom {

	public CartAtom(CartAtom old) {
		super(old);
	}

	public CartAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'a', 'r', 't' } );
	}
	
	public String getArtist() {
		return getStringMetadata();
	}

}
