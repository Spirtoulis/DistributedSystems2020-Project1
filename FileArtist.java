import com.mpatric.mp3agic.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileArtist { //klassi gia diavasma artist k dimiourgia txt me olous tus artist

    public static void main(String[] args) {
        TreeSet<String> artists = new TreeSet<String>();
        File dir = new File("C:\\Users\\teo\\Desktop\\dataset1\\dataset1\\Comedy");
        for (File file : dir.listFiles()) {
            try {
                Mp3File mp3 = new Mp3File(file);

                if (mp3.hasId3v1Tag()) {
                    ID3v1 id = mp3.getId3v1Tag();
                    if(!(id.getArtist() == null)) {
                        artists.add(id.getArtist());
                    }
                } else if (mp3.hasId3v2Tag()) {
                    ID3v2 id = mp3.getId3v2Tag();
                    if(!(id.getArtist() == null)) {
                        artists.add(id.getArtist());
                    }
                }

            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (UnsupportedTagException ut) {
                ut.printStackTrace();
            } catch (InvalidDataException d) {
                d.printStackTrace();
            }
        }


        System.out.println(artists.size());
        System.out.println(artists);

        Iterator i = artists.iterator();
        while(i.hasNext()){
            System.out.println(i.next());
        }
    }

}
