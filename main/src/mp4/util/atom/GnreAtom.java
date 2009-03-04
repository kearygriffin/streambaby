package mp4.util.atom;

public class GnreAtom extends AppleMetaAtom {

	public GnreAtom(GnreAtom old) {
		super(old);
	}

	public GnreAtom() {
		super(new byte[] { 'G', 'n', 'r', 'e' } );
	}
	
	public String getGenre() {
		return getStringMetadata();
	}

}
