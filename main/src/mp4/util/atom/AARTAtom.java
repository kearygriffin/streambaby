package mp4.util.atom;

public class AARTAtom extends AppleMetaAtom {

	public AARTAtom(AARTAtom old) {
		super(old);
	}

	public AARTAtom() {
		super(new byte[] { 'A', 'a', 'l', 'b' } );
	}
	
	public String getAlbumArtist() {
		return getStringMetadata();
	}
}
