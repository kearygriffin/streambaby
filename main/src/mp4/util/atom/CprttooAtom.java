package mp4.util.atom;

public class CprttooAtom extends AppleMetaAtom {

	public CprttooAtom(CprttooAtom old) {
		super(old);
	}

	public CprttooAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 't', 'o', 'o' } );
	}
	
	public String getCreator() {
		return getStringMetadata();
	}

}
