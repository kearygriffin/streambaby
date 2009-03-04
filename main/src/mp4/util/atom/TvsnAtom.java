package mp4.util.atom;

public class TvsnAtom extends AppleMetaAtom {

	public TvsnAtom(TvsnAtom old) {
		super(old);
	}

	public TvsnAtom() {
		super(new byte[] { 't', 'v', 's', 'n' } );
	}
}
