package mp4.util.atom;

public class KeywAtom extends AppleMetaAtom {

	public KeywAtom(KeywAtom old) {
		super(old);
	}

	public KeywAtom() {
		super(new byte[] { 'k', 'e', 'y', 'w' } );
	}
	
	public String getKeyword() {
		return getStringMetadata();
	}

}
