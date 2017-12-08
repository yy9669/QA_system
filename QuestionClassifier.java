package src;

import org.apdplat.word.segmentation.Word;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestionClassifier {
	
	static class PatternMatchResultItem {
	    public String type;
	    public String origin;
	    public String pattern;
	}
	
	static QuestionType transform(String type){
		if (type.contains("Person")){
			return QuestionType.PERSON_NAME;
		}
		else if (type.contains("Location")){
			return QuestionType.LOCATION_NAME;
		}
		else if (type.contains("Organization")){
			return QuestionType.ORGANIZATION_NAME;
		}
		else if (type.contains("Number")){
			return QuestionType.NUMBER;
		}
		else if (type.contains("Time")){
			return QuestionType.TIME;
		}
		else{
			return QuestionType.NULL;
		}
	}
	
	public static QuestionType classify(String question) {
		Pattern p_pre = Pattern.compile("上句|上一句|前句|前一句|前半句");
		Pattern p_next = Pattern.compile("下句|下一句|下半句|后一句|后半句|后句");
		Matcher m_pre = p_pre.matcher(question);
		Matcher m_next = p_next.matcher(question);
		if (m_pre.find()){
			return QuestionType.PRESENTENCE;
		}
		else if(m_next.find()){
			return QuestionType.NEXTSENTENCE;
		}
		List<String> questionPatterns = new ArrayList<>();
        question = question.trim();
        List<Word> words = WordParser.parse(question);
        StringBuilder termWithNatureStrs = new StringBuilder();
        StringBuilder natureStrs = new StringBuilder();
        int i = 0;
        for (Word word : words) {
            termWithNatureStrs.append(word.getText()).append("/").append(word.getPartOfSpeech().getPos()).append(" ");
            if ((i++) > 0) {
                natureStrs.append("/");
            }
            natureStrs.append(word.getPartOfSpeech().getPos());
        }
        String termWithNature = termWithNatureStrs.toString();
        String nature = natureStrs.toString();
        questionPatterns.add(termWithNature);
        questionPatterns.add(nature);
        File dir = new File("C:\\Users\\Ann\\Desktop\\AnswerRobot\\resource\\questionPatterns");
        String[] files = dir.list();
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(files));
        Collections.sort(list);
        //System.out.println(termWithNature);
        for (String qtfile : list){
        	String qtf = "C:\\Users\\Ann\\Desktop\\AnswerRobot\\resource\\questionPatterns\\" + qtfile;
            List<String> types = new ArrayList<>();
            List<Pattern> patterns = new ArrayList<>();
            BufferedReader reader = null;
            File file = new File(qtf);
            try{
            	InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            	reader = new BufferedReader(isr);
                String line = null;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0 || line.startsWith("//") || line.startsWith("#")) {
                        continue;
                    }
                    String[] tokens = line.split("\\s+", 3);
                    types.add(tokens[0]);
                    patterns.add(Pattern.compile(tokens[1], Pattern.CASE_INSENSITIVE));
                }
            }catch (Exception e) {}finally{
            	try{
            		reader.close();
            	}catch (Exception e) {}
            }
            int len = patterns.size();
            for (int j = 0; j < len; j++) {
                Pattern pattern = patterns.get(j);
                for (String questionPattern : questionPatterns) {
                    Matcher m = pattern.matcher(questionPattern);
                    if (m.matches()) {
                    	return transform(types.get(j));
                    }
                }
            }
        }
        return QuestionType.NULL;
    }
	
	/*public static void main(String[] args) {
		//System.out.println(classify("澳大利亚是南半球面积第几大的国家").getDes());
		//System.out.println(classify("马尔代夫的第一大支柱产业是什么？").getDes());
		//System.out.println(classify("阿根廷国家足球队赢得过多少次美洲杯冠军？").getDes());
		System.out.println(classify("平均人口密度最高的大洲是哪个洲？").getDes());
		//System.out.println(classify("中国历史上最杰出的浪漫主义诗人是").getDes());
	}*/

}
