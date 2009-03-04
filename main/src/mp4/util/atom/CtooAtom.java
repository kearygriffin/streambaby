package mp4.util.atom;

public class CtooAtom extends AppleMetaAtom {

	public CtooAtom(CtooAtom old) {
		super(old);
	}

	public CtooAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 't', 'o', 'o' } );
	}
	
	public String getCreator() {
		return getStringMetadata();
	}

}
