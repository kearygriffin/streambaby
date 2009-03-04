package mp4.util.atom;

public class DescAtom extends AppleMetaAtom {

	public DescAtom(DescAtom old) {
		super(old);
	}

	public DescAtom() {
		super(new byte[] { 'd', 'e', 's', 'c' } );
	}
	
	public String getDescription() {
		return getStringMetadata();
	}

}
