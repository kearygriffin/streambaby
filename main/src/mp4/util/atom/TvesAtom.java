package mp4.util.atom;

public class TvesAtom extends AppleMetaAtom {

	public TvesAtom(TvesAtom old) {
		super(old);
	}

	public TvesAtom() {
		super(new byte[] { 't', 'v', 'e', 's' } );
	}
}
