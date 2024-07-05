package com.example.mbmeditorsmt4;

import java.util.ArrayList;

public class Format {
    private String codeTextFormat;
    private String speakerName;
    private ArrayList<String> correctTextFormat;

    public Format(String codeTextFormat) {
        this.codeTextFormat = codeTextFormat;
        this.correctTextFormat = new ArrayList<>();
        formatText();
    }

    public String getCodeTextFormat() {
        return codeTextFormat;
    }

    public ArrayList<String> getCorrectTextFormat() {
        return correctTextFormat;
    }

    public String getSpeakerName() {
        return speakerName;
    }

    public void setCodeTextFormat(String codeTextFormat) {
        this.codeTextFormat = codeTextFormat;
        formatText();
    }

    public void setCorrectTextFormat(ArrayList<String> correctTextFormat) {
        this.correctTextFormat = correctTextFormat;
    }

    public void setSpeakerName(String speakerName) {
        this.speakerName = speakerName;
    }

    private void formatText() {
        if (codeTextFormat == null || codeTextFormat.length() < 7) {
            String TextFormat = codeTextFormat;
            return;
        }

        String TextFormat = codeTextFormat;
        if(codeTextFormat.endsWith("{F801}")){
            TextFormat = TextFormat.substring(0, codeTextFormat.length() - 6);
        }
        TextFormat = TextFormat.replace("{F801}{F802}", "(NewDialogBox)");
        TextFormat = TextFormat.replace("{F801}", "\n");
        TextFormat = TextFormat.replace("{F843,0}", "(Protag)");
        TextFormat = TextFormat.replace("{F804,5}", "(Color:red)");
        TextFormat = TextFormat.replace("{F804,1}", "(Color:blue)");
        TextFormat = TextFormat.replace("{F804,0}", "(Color:black)");
        TextFormat = TextFormat.replace("{F871,1}", "");

        if (TextFormat.startsWith("{F812}")) {
            int endIdx = TextFormat.indexOf("\\0");
            if (endIdx != -1) {
                speakerName = TextFormat.substring(6, endIdx);
                TextFormat = TextFormat.replace("{F812}", "");
                TextFormat = TextFormat.replace(speakerName, "");
                TextFormat = TextFormat.replace("\\0", "");
                TextFormat = TextFormat.replace("{F87A,0}", "");
                TextFormat = TextFormat.replaceAll("\\{F87A,\\d+\\}", "");
                TextFormat = TextFormat.replaceAll("\\{F87B,\\d+,\\d+,\\d+,\\d+\\}", "");
                TextFormat = TextFormat.replaceAll("\\{F813,0,\\d+\\}", "");
            } else {
                speakerName = "";
            }
        }
        addToList(TextFormat);
    }

    private void addToList(String TextFormat) {
        if (correctTextFormat == null) {
            correctTextFormat = new ArrayList<>();
        }
        String[] parts = TextFormat.split("\\(NewDialogBox\\)");
        for(String part : parts){
            correctTextFormat.add(part.trim());
        }
    }
}
