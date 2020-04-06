public class Value {

    private MusicFile musicFile;

    public Value(){
        musicFile = null;
    }

    public Value(MusicFile m_file){
        musicFile = m_file;
    }

    public void setMusicFile(MusicFile musicFile){
        this.musicFile = musicFile;
    }

    public MusicFile getMusicFile() {
        return musicFile;
    }
}