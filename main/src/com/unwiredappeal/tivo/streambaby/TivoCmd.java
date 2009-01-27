package com.unwiredappeal.tivo.streambaby;

import java.io.IOException;
import java.lang.reflect.Field;

import com.tivo.hme.sdk.Application;
import com.tivo.hme.sdk.io.ChunkedOutputStream;

public class TivoCmd {
	public Application app;
	ChunkedOutputStream out;
	public TivoCmd(Application app) {
		this.app = app;
		Field f = null;
		try {
			Field[] flds = Application.class.getDeclaredFields();
			for (int i=0;i<flds.length;i++) {
				if (flds[i].getName().compareTo("out") == 0) {
					f = flds[i];
					break;
				}
			}
			//f = app.getClass().getField("out");
			f.setAccessible(true);
			out = (ChunkedOutputStream)f.get(app);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

    //
    // Following three helper methods are called from synchronized methods
    //
    
    private void writeCommand(int opcode, int id) throws IOException
    {
        out.writeVInt(opcode);
        out.writeVInt(id);
    }

    private void writeTerminator() throws IOException
    {
        out.writeTerminator();

    }

    public void sendCmd(int id, int cmd, int arg)
    {
        try {
            writeCommand(cmd, id);
            if (arg != -1)
            	out.writeVInt(arg);
            writeTerminator();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
