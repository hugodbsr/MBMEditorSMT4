package interfaceEditor;

import org.fxmisc.richtext.InlineCssTextArea;

public class TextUtils {

    public static void updateTextEntryStyle(InlineCssTextArea textEntry, String text, int start, int end, String color) {
        textEntry.setStyle(start, end, "-fx-fill: " + color);
    }

    public static void applyColor(InlineCssTextArea textEntry) {
            String text = textEntry.getText();
        textEntry.setStyle(0, text.length(), "-fx-fill: black");
            boolean isRED = false;
            if(text.contains("£") || text.contains("µ")){
                for (int i = 0; i < text.length(); i++) {
                    isRED = text.charAt(i) == '£' || (text.charAt(i) != 'µ' && isRED);
                    if(text.charAt(i) == '£' || text.charAt(i) == 'µ') {
                        int start = i;
                        textEntry.replaceText(i, i+1, "");
                        text = textEntry.getText();
                        while (i < text.length() && text.charAt(i) != '§') {
                            i++;
                        }
                        int end = i;
                        textEntry.replaceText(i, i+1, "");
                        text = textEntry.getText();
                        if(isRED){
                            textEntry.setStyle(start, end,"-fx-fill: red");
                        }
                        else{
                            textEntry.setStyle(start, end,"-fx-fill: blue");
                        }
                    }
                }
            }
            else{
                textEntry.setStyle(0, text.length(), "-fx-fill: black");
            }
        }

    public static int adjustPosition(String text, int position) {
        int cpt = 0;
        for (int i = 0; i < position; i++) {
            if (text.charAt(i) == '£' || text.charAt(i) == '§' || text.charAt(i) == 'µ') {
                cpt++;
            }
        }
        return position + cpt;
    }
}

