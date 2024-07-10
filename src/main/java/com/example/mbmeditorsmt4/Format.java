package com.example.mbmeditorsmt4;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class Format {
    private String codeTextFormat;
    private String ORIGINALcodeTextFormat;
    private String speakerName;
    private String ORIGINALspeakerName;
    private ArrayList<String> correctTextFormat;
    private ArrayList<String> ORIGINALcorrectTextFormat;

    public Format(String codeTextFormat, String ORIGINALcodeTextFormat) {
        this.ORIGINALcodeTextFormat = ORIGINALcodeTextFormat;
        this.codeTextFormat = codeTextFormat;
        this.correctTextFormat = new ArrayList<>();
        this.ORIGINALcorrectTextFormat = new ArrayList<>();
        formatText(codeTextFormat, correctTextFormat, speakerName);
        formatText(ORIGINALcodeTextFormat, ORIGINALcorrectTextFormat, ORIGINALspeakerName);
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

    public ArrayList<String> getORIGINALcorrectTextFormat() {
        return ORIGINALcorrectTextFormat;
    }

    public String getORIGINALcodeTextFormat() {
        return ORIGINALcodeTextFormat;
    }

    public String getORIGINALspeakerName() {
        return ORIGINALspeakerName;
    }

    public void setCodeTextFormat(String codeTextFormat) {
        this.codeTextFormat = codeTextFormat;
        formatText(codeTextFormat, correctTextFormat, speakerName);
    }

    public void setCorrectTextFormat(ArrayList<String> correctTextFormat) {
        this.correctTextFormat = correctTextFormat;
    }

    public void setSpeakerName(String speakerName) {
        this.speakerName = speakerName;
    }

    private void formatText(String codeTextFormat, ArrayList<String> correctTextFormat, String name) {
        if (codeTextFormat == null || codeTextFormat.length() < 7) {
            return;
        }

        String TextFormat = codeTextFormat;
        if(codeTextFormat.endsWith("{F801}")){
            TextFormat = TextFormat.substring(0, codeTextFormat.length() - 6);
        }
        TextFormat = TextFormat.replace("{F801}{F802}", "(NewDialogBox)");
        TextFormat = TextFormat.replace("{F801}", "\n");
        TextFormat = TextFormat.replace("{F843,0}", "(Protag)");
        TextFormat = TextFormat.replace("{F873,1}", "(Demon)");
        TextFormat = TextFormat.replace("{F873,2}", "(Demon2)");
        TextFormat = TextFormat.replace("{F875,1}", "(Race)");
        TextFormat = TextFormat.replace("{F877,1}", "(Skill)");
        TextFormat = TextFormat.replace("{F877,2}", "(Skill2)");
        TextFormat = TextFormat.replace("{F872,1}", "(Object)");
        TextFormat = TextFormat.replace("{F878,1}", "(Quantity)");
        TextFormat = TextFormat.replace("{F87D,14}", "(Attack Type)");
        TextFormat = TextFormat.replace("{F87D,15}", "(Target Type)");
        TextFormat = TextFormat.replace("{F873,6}", "(Ally)");
        TextFormat = TextFormat.replace("{F804,5}", "£");//red
        TextFormat = TextFormat.replace("{F804,1}", "µ");//blue
        TextFormat = TextFormat.replace("{F804,0}", "§");//black
        TextFormat = TextFormat.replace("{F871,1}", "");

        if (TextFormat.startsWith("{F812}")) {
            int endIdx = TextFormat.indexOf("\\0");
            if (endIdx != -1) {
                name = TextFormat.substring(6, endIdx);
                TextFormat = TextFormat.replace("{F812}", "");
                TextFormat = TextFormat.replace(name, "");
                TextFormat = TextFormat.replace("\\0", "");
                TextFormat = TextFormat.replace("{F87A,0}", "");
                TextFormat = TextFormat.replaceAll("\\{F87A,\\d+\\}", "");
                TextFormat = TextFormat.replaceAll("\\{F87B,\\d+,\\d+,\\d+,\\d+\\}", "");
                TextFormat = TextFormat.replaceAll("\\{F813,\\d+,\\d+\\}", "");
            } else {
                name = "";
            }
        }
        addToList(TextFormat, correctTextFormat);
    }

    private void addToList(String TextFormat, ArrayList<String> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        String[] parts = TextFormat.split("\\(NewDialogBox\\)");
        for(String part : parts){
            list.add(part.trim());
        }
    }

    public void convertToCode(){
        String TextFormat = "";
        String name = "";
        int idx = 0;
        if(codeTextFormat.startsWith("{F812}")) {
            String start = codeTextFormat.substring(0, 6);
            name = start + getSpeakerName() + "\\0";
            idx = codeTextFormat.indexOf("\\0")+2;
        }
        for(int i = 0; i < correctTextFormat.size(); i++){
            if(codeTextFormat.charAt(idx) == '{' && !codeTextFormat.startsWith("{F801}") && idx != 0){
                while(codeTextFormat.charAt(idx) != '}'){
                    TextFormat = TextFormat + codeTextFormat.charAt(idx);
                    idx++;
                    if(codeTextFormat.charAt(idx) == '}' && idx < codeTextFormat.length()-1){
                        if(codeTextFormat.charAt(idx+1) == '{'){
                            if(!codeTextFormat.startsWith("{F801}", idx+1)){
                                TextFormat = TextFormat + codeTextFormat.charAt(idx);
                                idx++;
                            }
                        }
                    }
                }
                TextFormat = TextFormat + "}";
            }
            else if(codeTextFormat.startsWith("{F871,1}")){
                TextFormat += "{F871,1}";
            }
            TextFormat = TextFormat + correctTextFormat.get(i).replace("\n", "{F801}");
            if(i<correctTextFormat.size()-1){
                String temporary = codeTextFormat.substring(idx);
                idx = idx + temporary.indexOf("{F801}{F802}");
            }
        }

        TextFormat = TextFormat.replace("(Protag)", "{F843,0}");
        TextFormat = TextFormat.replace("(Demon)", "{F873,1}");
        TextFormat = TextFormat.replace("(Demon2)", "{F873,2}");
        TextFormat = TextFormat.replace("(Race)", "{F875,1}");
        TextFormat = TextFormat.replace("(Skill)", "{F877,1}");
        TextFormat = TextFormat.replace("(Skill2)", "{F877,2}");
        TextFormat = TextFormat.replace("(Object)", "{F872,1}");
        TextFormat = TextFormat.replace("(Quantity)", "{F878,1}");
        TextFormat = TextFormat.replace("(Attack Type)", "{F87D,14}");
        TextFormat = TextFormat.replace("(Target Type)", "{F87D,15}");
        TextFormat = TextFormat.replace("(Ally)", "{F873,6}");
        TextFormat = TextFormat.replace("£", "{F804,5}");//red
        TextFormat = TextFormat.replace("µ", "{F804,1}");//blue
        TextFormat = TextFormat.replace("§", "{F804,0}");//black
        if(codeTextFormat.endsWith("{F802}")){
            codeTextFormat = name + TextFormat + "{F801}" + "{F802}";
        }
        else{
            codeTextFormat = name + TextFormat + "{F801}";
        }
    }
}
