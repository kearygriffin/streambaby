package mp4.util.atom;

public class TvenAtom extends AppleMetaAtom {

	public TvenAtom(TvenAtom old) {
		super(old);
	}

	public TvenAtom() {
		super(new byte[] { 't', 'v', 'e', 'n' } );
	}
	
	public String getEpisodeNumber() {
		return getStringMetadata();
	}

}
