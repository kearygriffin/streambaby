package mp4.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;


import mp4.util.atom.Atom;
import mp4.util.atom.AtomException;
import mp4.util.atom.ContainerAtom;
import mp4.util.atom.DefaultAtomVisitor;
import mp4.util.atom.FtypAtom;
import mp4.util.atom.LeafAtom;
import mp4.util.atom.MdatAtom;
import mp4.util.atom.MoovAtom;
import mp4.util.atom.TrakAtom;
import mp4.util.atom.UnknownAtom;


/**
 * This class is used to split an mpeg4 file.
 *
 * The mpeg4 file format is the 
 * <a href="http://developer.apple.com/DOCUMENTATION/QuickTime/QTFF/qtff.pdf">Quicktime format</a>
 * 
 * The mpeg4 format is also ISO standard 14496-12.  That is, Part 12 of
 * the ISO mpeg4 specificatio.
 * 
 * Splitting the mpeg4 file requires rewritting the stbl atom container
 * with new data and cutting off the mdat section.
 */
public class Mp4Split extends DefaultAtomVisitor {
  // the input mp4 file
  protected DataInputStream mp4file;
  
  //public static String inputFile;
  //public static String outputFile;

	public String inputFile;
	public String outputFile;
	public boolean writeMdat = true;
	public static float time;
	protected FtypAtom ftyp;
	protected MoovAtom moov;
	protected MdatAtom mdat;
	protected MoovAtom cutMoov;
	protected MdatAtom cutMdat;
	Mp4InterleaveWriter iwriter = null;
	boolean lastAtom = false;
  
  @Override
  protected void defaultAction(Atom atom) throws AtomException {
    if (atom.isContainer()) {
      long bytesRead = 0;
      long bytesToRead = atom.dataSize();
      while (bytesRead < bytesToRead) {
        Atom child = parseAtom();
        ((ContainerAtom)atom).addChild(child);
        bytesRead += child.size();
      }
    }
    else {
      // the default action for a leaf is to read the data in to a buffer
      ((LeafAtom)atom).readData(mp4file);
    }
  }

  /**
   * Don't the the mdat atom since that's the biggest segment of the 
   * file.  It contains the video and sound data.  Plus, we'll just
   * skip over the beginning when we cut the movie.
   */
  @Override
  public void visit(MdatAtom atom) throws AtomException {
    atom.setInputStream(mp4file);
    try {
    	if (atom.dataSize() != 0 && mp4file.markSupported())
    		mp4file.skip(atom.dataSize());
    	else
    		lastAtom = true;
    } catch(IOException e) {
    	throw new AtomException(e.getMessage());
    }
  }
  
	/**
	 * Parse an atom from the mpeg4 file.
	 * 
	 * @return the number of bytes read
	 * @throws AtomException
	 */
	private Atom parseAtom() throws AtomException {
		// get the atom size
		if (lastAtom)
			throw new AtomException("Already parsed last atom!");
		byte[] word = new byte[Atom.ATOM_WORD];
		int num;
		try {
			num = mp4file.read(word);
		} catch (IOException e1) {
			throw new AtomException("IOException while reading file");
		}
		// check for end of file
		if (num == -1) {
			return null;
		}
		if (num != Atom.ATOM_WORD) {
			throw new AtomException("Unable to read enough bytes for atom");
		}
		long size = Atom.byteArrayToUnsignedInt(word, 0);
		// get the atom type
		try {
			num = mp4file.read(word);
		} catch (IOException e1) {
			throw new AtomException("IOException while reading file");
		}
		if (num != Atom.ATOM_WORD) {
			throw new AtomException("Unable to read enough bytes for atom");
		}
		try {
			Atom atom;
			try {
				Class<?> cls = Class.forName(Atom.typeToClassName(word));
				atom = (Atom) cls.newInstance();
			} catch (ClassNotFoundException e) {
				MP4Log.log("UnknownAtom: " + Atom.typeToClassName(word));
				//if (word[2] == 'h' && word[3] == 'd')
					//atom = new UnkHdAtom(word);
				//else
					atom = new UnknownAtom(word);
			}

			atom.setSize(size);
			atom.accept(this);
			return atom;
		} catch (InstantiationException e) {
			throw new AtomException("Unable to instantiate atom");
		} catch (IllegalAccessException e) {
			throw new AtomException("Unabel to access atom object");
		}
	}
	

  /**
   * Constructor for the Mpeg-4 file splitter.  It opens the 
   * @param fn
 * @throws IOException 
   */
	/*
  public Mp4Split(String fn) {
    try {
      mp4file = new DataInputStream(new FileInputStream(fn));
      MP4Log.log("DBG: file size " + new File(fn).length());
    } catch (FileNotFoundException e) {
      MP4Log.log("File not found " + fn);
      System.exit(-1);
    }
  }
  */

	public void writeSplitMp4(DataOutputStream dos) throws IOException {
		ftyp.writeData(dos);
		if (iwriter != null) {
			iwriter.write(dos, writeMdat);
		} else {
			cutMoov.writeData(dos);
			if (this.writeMdat) {
				cutMdat.writeData(dos);
			}
		}
	}

	public void calcSplitMp4() throws IOException {
		calcSplitMp4(false);
	}
	public void calcSplitMp4(boolean reinterleave) throws IOException {
		long mdatOffset = 0;
		try {
			long offset = 0;
			while (ftyp == null || moov == null || mdat == null) {
				Atom atom = parseAtom();
				if (atom == null) {
					throw new IOException("Couldn't find all required MP4 atoms");
				}
				if (atom instanceof FtypAtom)
					ftyp = (FtypAtom) atom;
				else if (atom instanceof MoovAtom)
					moov = (MoovAtom) atom;
				else if (atom instanceof MdatAtom) {
					mdatOffset = offset;
					mdat = (MdatAtom) atom;
				}
				offset += atom.size();
			}
			// ftyp = (FtypAtom) parseAtom();
			// moov = (MoovAtom) parseAtom();
			// mdat = (MdatAtom) parseAtom();

			MP4Log.log("DBG: moov size " + moov.dataSize());
			MP4Log.log("DBG: mdat size " + mdat.dataSize());

			// Remove all but one video, one audio track
			// this makes some things a little easier
			boolean hasVideo = false;
			boolean hasAudio = false;
			Iterator<TrakAtom> it = moov.getTracks();
			while(it.hasNext()) {
				TrakAtom trak = it.next();
				if (trak.getMdia().getHdlr().isVideo()) {
					if (hasVideo)
						it.remove();
					else
						hasVideo = true;
				} else if (trak.getMdia().getHdlr().isSound()) {
					if (hasAudio)
						it.remove();
					else
						hasAudio = true;
				} else
					it.remove();
			}

			//float common = moov.findAdjustedTime(time);
			cutMoov = moov.cut(time);
			
			// Remove the IODS atom
			// because it may point to tracks we have already removed
			cutMoov.setIods(null);
			cutMoov.recomputeSize();
			
			MP4Log.log("DBG: moov chunk " + moov.firstDataByteOffset());
			MP4Log.log("DBG: cut moov chunk "
					+ cutMoov.firstDataByteOffset());
			long mdatSkip = cutMoov.firstDataByteOffset()
					- moov.firstDataByteOffset();
			cutMdat = mdat.cut(mdatSkip);

			// update stco segment by mdatSkip + difference in moov size
			long newMdatOffset = ftyp.size() + cutMoov.size();
			long updateAmount = mdatSkip + (mdatOffset - newMdatOffset);
			//long updateAmount = mdatSkip + (moov.size() - cutMoov.size());

			MP4Log.log("DBG: updateAmount " + updateAmount);
			cutMoov.fixupOffsets(-updateAmount);

			MP4Log.log("DBG: movie skip " + mdatSkip);

			MP4Log.log("DBG: Cut Movie time "
					+ cutMoov.getMvhd().getDurationNormalized() + " sec ");

			/*
			 * DataOutputStream dos = new DataOutputStream(new
			 * FileOutputStream(outputFile)); ftyp.writeData(dos);
			 * cutMoov.writeData(dos); if (Mp4Split.mdat) {
			 * cutMdat.writeData(dos); }
			 */

			if (reinterleave) {
				iwriter = new Mp4InterleaveWriter(cutMoov, cutMdat, ftyp.size() + cutMoov.size() + Atom.ATOM_HEADER_SIZE);
				iwriter.calcInterleave();
			}


		} catch (AtomException e) {
			MP4Log.log("Error parseing Mp4 file " + e);
			throw new IOException(e.getMessage());
		}
	}

	/*
  public void splitMp4() {
    try {
      FtypAtom ftyp = (FtypAtom) parseAtom();
      MoovAtom moov = (MoovAtom) parseAtom();
      MdatAtom mdat = (MdatAtom) parseAtom();
      
      MP4Log.log("DBG: moov size " + moov.dataSize());
      MP4Log.log("DBG: mdat size " + mdat.dataSize());
  
      MoovAtom cutMoov = moov.cut(time);
      MP4Log.log("DBG: moov chunk " + moov.firstDataByteOffset());
      MP4Log.log("DBG: cut moov chunk " + cutMoov.firstDataByteOffset());
      long mdatSkip = cutMoov.firstDataByteOffset() - moov.firstDataByteOffset();
      MdatAtom cutMdat = mdat.cut(mdatSkip);
      
      // update stco segment by mdatSkip + difference in moov size
      long updateAmount = mdatSkip + (moov.size() - cutMoov.size());
      
      MP4Log.log("DBG: updateAmount " + updateAmount);
      cutMoov.fixupOffsets(-updateAmount);
      
      MP4Log.log("DBG: movie skip " + mdatSkip);
      
      MP4Log.log("DBG: Cut Movie time " + cutMoov.getMvhd().getDurationNormalized() + " sec ");
      
      DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile));
      ftyp.writeData(dos);
      cutMoov.writeData(dos);
      if (Mp4Split.mdat) {
        cutMdat.writeData(dos);
      }
      
     } catch (AtomException e) {
      MP4Log.log("Error parseing Mp4 file " + e);
    } catch (FileNotFoundException e) {
      MP4Log.log("Error creating file output stream");
    } catch (IOException e) {
      MP4Log.log("Error writing output ");
      e.printStackTrace();
    }
  }
  */
	
  /**
   * Process the command line arguments.
   * @param args the user-specified arguments
   */
  private void processArgs(String[] args) {
    int i = 0;
    while (i < args.length) {
      String arg = args[i];
      if (arg.equals("-in")) {
        inputFile = args[++i];
      }
      else if (arg.equals("-out")) {
        outputFile = args[++i];
      }
      else if (arg.equals("-time")) {
        time = Float.valueOf(args[++i]);
      }
      else if (arg.equals("-no_mdat")) {
        writeMdat = false;
      }
      else {
        help();
      }
      i++;
    }
    if (inputFile == null) {
      help();
    }
  }
  
  private static void help() {
    MP4Log.log("Mp4Split <args>");
    MP4Log.log("  -in <inputfile.mp4>");
    MP4Log.log("  -out <outputfile.mp4>");
    MP4Log.log("  -time <seconds>");
    MP4Log.log("  [-no_mdat]");
    System.exit(-1);
  }
  
  public void runCmdLine(String[] args)  {
	processArgs(args);
    try {
		mp4file = new DataInputStream(new FileInputStream(new File(inputFile)));
    	calcSplitMp4();
	    writeSplitMp4(new DataOutputStream(new FileOutputStream(new File(outputFile))));
	} catch (FileNotFoundException e) {
		MP4Log.log("Err: FileNoutFound: " + e.getMessage());
	} catch (IOException e) {
		MP4Log.log("Err: IOException: " + e.getMessage());
	}	
	MP4Log.log("Complete.");
  }
    
  /**
   * @param args
   */
  public static void main(String[] args) {
    Mp4Split splitter = new Mp4Split();
    splitter.runCmdLine(args);
  }

}
