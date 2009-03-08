package mp4.util.atom;

public class CprtgenAtom extends AppleMetaAtom {

	public CprtgenAtom(CprtgenAtom old) {
		super(old);
	}

	public CprtgenAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'g', 'e', 'n' } );
		
		}
	public String getGenre() {
		return getStringMetadata();

	}
}
