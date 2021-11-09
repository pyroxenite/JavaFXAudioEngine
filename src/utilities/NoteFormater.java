package utilities;

final public class NoteFormater {
    public static String[] NOTES = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

    public static String numberToText(int note) {
        if (note < 24) return "";
        int octave = (note-24)/12 + 1;
        String noteName = NOTES[(note-24)%12];
        return noteName + octave;
    }

    public static void main(String[] args) {
        System.out.println(NoteFormater.numberToText(27));
    }
}
