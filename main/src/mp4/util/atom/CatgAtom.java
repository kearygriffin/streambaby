package mp4.util.atom;

public class CatgAtom extends AppleMetaAtom {

	public CatgAtom(CatgAtom old) {
		super(old);
	}

	public CatgAtom() {
		super(new byte[] { 'c', 'a', 't', 'g' } );
	}
	
	public String getCategory() {
		return getStringMetadata();
	}

}
