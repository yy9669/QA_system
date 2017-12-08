package src;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ann on 2016/12/4.
 */
public class MainSystem {
    public final List<Question> unknownTypeQuestions = new ArrayList<>();

    public QuestionClassifier questionClassifier;
    public DataSource dataSource;
    public CandidateAnswerSelect candidateAnswerSelect;
    //public CandidateAnswerScore candidateAnswerScore;

    public void setQuestionClassifier(QuestionClassifier questionClassifier) {this.questionClassifier = questionClassifier;}
    public QuestionClassifier getQuestionClassifier() {return this.questionClassifier;}

    public void setDataSource(DataSource dataSource) {this.dataSource = dataSource;}
    public DataSource getDataSource() {return this.dataSource;}

    public void setCandidateAnswerSelect(CandidateAnswerSelect candidateAnswerSelect) {this.candidateAnswerSelect = candidateAnswerSelect;}
    public CandidateAnswerSelect getCandidateAnswerSelect() {return this.candidateAnswerSelect;}

    //public void setCandidateAnswerScore(CandidateAnswerScore candidateAnswerScore) {this.candidateAnswerScore = candidateAnswerScore;}
    //public CandidateAnswerScore getCandidateAnswerScore() {return this.candidateAnswerScore;}

    public List<Question> answerQuestions() {return this.dataSource.getAndAnswerQuestions(this);}//调用dataSource中函数获取并回答问题

    public List<Question> answerQuestions(List<Question> questions,File file) {//数据类型是question，该函数从原始question到分类、答案都找好的question，dataSource中函数会调用来回答问题

        for (Question question : questions) {
			try{
				FileWriter fileWriter = new FileWriter(file,true);
				fileWriter.write(String.valueOf(question.num)+"\t");
				fileWriter.close();
			}
			catch (IOException e) {e.printStackTrace();}
            question.questionType= questionClassifier.classify(question.question);

            /*if (question.questionType == QuestionType.NULL) {
                unknownTypeQuestions.add(question);//未知类型
                try{
                    FileWriter fileWriter = new FileWriter(file,true);
                    fileWriter.write("I don't know");
                    fileWriter.close();
                }
                catch (IOException e) {e.printStackTrace();}
                continue;
            }*/
            if (question.questionType == QuestionType.PRESENTENCE || question.questionType == QuestionType.NEXTSENTENCE){
            	Pattern p = Pattern.compile("[“\\\"](.*?)[”\\\"]");
            	Matcher m = p.matcher(question.question);
            	if (m.find()){
            		String half = m.group(1);
            		if (question.questionType == QuestionType.NEXTSENTENCE){
            			Pattern p_next = Pattern.compile(half+"[,，\\?？!！\\s—]—*\\s*(\\S*?)\\s*[,，\\.。\\?？!！”\\\"\\s$]",Pattern.MULTILINE);
            			for (Evidence evidence : question.evidences){
            				Matcher m_next = p_next.matcher(evidence.title + "," + evidence.snippet);
            				if (m_next.find()){
            					CandidateAnswer answer = new CandidateAnswer();
            	                answer.answer = m_next.group(1);
            	                if (!evidence.candidateAnswerCollection.contains(answer)) {
            	                    evidence.candidateAnswerCollection.add(answer);
            	                }
            				}
            			}
            		}
            		else if (question.questionType == QuestionType.PRESENTENCE){
            			Pattern p_pre = Pattern.compile("[,，\\.。\\?？!！\\\"“\\s^](\\S*?)[,，\\?？!！\\s—]—*\\s*"+half,Pattern.MULTILINE);
            			for (Evidence evidence : question.evidences){
            				Matcher m_next = p_pre.matcher(evidence.title + "," + evidence.snippet);
            				if (m_next.find()){
            					CandidateAnswer answer = new CandidateAnswer();
            	                answer.answer = m_next.group(1);
            	                if (!evidence.candidateAnswerCollection.contains(answer)) {
            	                    evidence.candidateAnswerCollection.add(answer);
            	                }
            				}
            			}
            		}
            	}
            	else{
            		question.questionType = QuestionType.NULL;
            	}
            }
            boolean flag = false;
            List<String> good_answers = new ArrayList<>();
            for (Evidence evidence : question.evidences) {//这些证据已经被评好分了
            	if ((evidence.title + "," + evidence.snippet).contains("一站到底")){
            		//System.out.println(evidence.title + "," + evidence.snippet);
            		//System.out.println(question.question);
            		String tmp_str = question.question.replace("，", "[,，]");
            		Pattern p0 = Pattern.compile(tmp_str+"[\\?？](.*?)(\\s|(\\.\\.\\.))");
            		Matcher m0 = p0.matcher(evidence.snippet);
            		while(m0.find()){
            			/*System.out.println(m0.group(2));
            			try{
    	                    FileWriter fileWriter = new FileWriter(file,true);
    	                    fileWriter.write(m0.group(2));
    	                    fileWriter.close();
    	                }
    	                catch (IOException e) {e.printStackTrace();}*/
            			good_answers.add(m0.group(1));
    					flag = true;
            		}
            	}
            	Pattern p = Pattern.compile("最佳答案[:：]\\s*(.*?)\\s*[,，\\.。!！\\s$]",Pattern.MULTILINE);
            	Matcher m = p.matcher(evidence.title + "," + evidence.snippet);
				if (m.find()){
					CandidateAnswer answer = new CandidateAnswer();
	                answer.answer = m.group(1);
	                //flag = true;
	                if (!evidence.candidateAnswerCollection.contains(answer)) {
	                    evidence.candidateAnswerCollection.add(answer);
	                }
				}
				/*else if (flag){
					continue;
				}*/
                candidateAnswerSelect.select(question, evidence);
                //从evidence对象里面获得候选答案
                List<CandidateAnswer> candidateAnswerCollection = evidence.getCandidateAnswerCollection();
                if (!candidateAnswerCollection.isEmpty()) {
                    candidateAnswerSelect.TermDistanceMiniCandidateAnswerScore(question, evidence);
                }
            }
            if (flag){
            	String final_answer = "";
            	int len = 0;
            	for(String answer: good_answers){
            		if (answer.length() > len&&!answer.contains("答案")){
            			len = answer.length();
            			final_answer = answer;
            		}
            	}
				Pattern p=Pattern.compile("(.*?)\\d+[、.]");
				Matcher m=p.matcher(final_answer);
				if(m.find()){
					final_answer=m.group(1);
				}
            	System.out.println(final_answer);
    			try{
                    FileWriter fileWriter = new FileWriter(file,true);
                    fileWriter.write(final_answer+"\n");
                    fileWriter.close();
                }
                catch (IOException e) {e.printStackTrace();}
            	continue;
            }

			List<CandidateAnswer> aa=question.getAllCandidateAnswer();
            if(aa.isEmpty()){
            	try{
                    FileWriter fileWriter = new FileWriter(file,true);
                    fileWriter.write("I don't know!"+"\n");
                    fileWriter.close();
                }
                catch (IOException e) {e.printStackTrace();}
            	continue;
            }


            if(aa.size()>1&&aa.get(1).score>0.7){
            	Pattern p1 = Pattern.compile(aa.get(0).answer+".{0,1}"+aa.get(1).answer);
            	Pattern p2 = Pattern.compile(aa.get(1).answer+".{0,1}"+aa.get(0).answer);
            	for (Evidence evidence : question.evidences){
            		Matcher m1 = p1.matcher(evidence.title + "," + evidence.snippet);
            		Matcher m2 = p2.matcher(evidence.title + "," + evidence.snippet);
    				if (m1.find()){
    					System.out.println(m1.group(0));
    					try{
    	                    FileWriter fileWriter = new FileWriter(file,true);
    	                    fileWriter.write(m1.group(0)+"\n");
    	                    fileWriter.close();
    	                }
    	                catch (IOException e) {e.printStackTrace();}
    					flag = true;
    					break;
    				}
    				if (m2.find()){
    					System.out.println(m2.group(0));
    					try{
    	                    FileWriter fileWriter = new FileWriter(file,true);
    	                    fileWriter.write(m2.group(0)+"\n");
    	                    fileWriter.close();
    	                }
    	                catch (IOException e) {e.printStackTrace();}
    					flag = true;
    					break;
    				}
				}
            }
            if (flag){
            	continue;
            }
            for (CandidateAnswer answer : aa){
            	System.out.println(answer.answer);
            	System.out.println(answer.score);
            }
            try{
                FileWriter fileWriter = new FileWriter(file,true);
                fileWriter.write(aa.get(0).answer+"\n");
                fileWriter.close();
            }
            catch (IOException e) {e.printStackTrace();}
        }
        return questions;
    }

    public static void main(String[] args) {

		PrintStream ps = null;
		try{
			ps = new PrintStream(new FileOutputStream("C:\\Users\\Ann\\Desktop\\debug.txt"));
			System.setOut(ps);

		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}

        //2、问答系统数据源
        String files="C:\\Users\\Ann\\Desktop\\test.txt";
        DataSource dataSource = new DataSource(files,1);//0表示读入字符串而不是文件

        //3、候选答案提取器
        CandidateAnswerSelect candidateAnswerSelect = new CandidateAnswerSelect();

        //5、候选答案评分组件(可以同时使用多个组件,这里根据我们时间考虑用几个)
        //5.1、词频评分组件
//        CandidateAnswerScore termFrequencyCandidateAnswerScore = new TermFrequencyCandidateAnswerScore();
//        termFrequencyCandidateAnswerScore.setScoreWeight(scoreWeight);
//        //5.2、词距评分组件
//        CandidateAnswerScore termDistanceCandidateAnswerScore = new TermDistanceCandidateAnswerScore();
//        termDistanceCandidateAnswerScore.setScoreWeight(scoreWeight);
//        //5.3、词距评分组件(只取候选词和问题词的最短距离)
//        CandidateAnswerScore termDistanceMiniCandidateAnswerScore = new TermDistanceMiniCandidateAnswerScore();
//        termDistanceMiniCandidateAnswerScore.setScoreWeight(scoreWeight);
//        //5.4、文本对齐评分组件
//        CandidateAnswerScore textualAlignmentCandidateAnswerScore = new TextualAlignmentCandidateAnswerScore();
//        textualAlignmentCandidateAnswerScore.setScoreWeight(scoreWeight);
//        //5.5、文本对齐评分组件
//        CandidateAnswerScore moreTextualAlignmentCandidateAnswerScore = new MoreTextualAlignmentCandidateAnswerScore();
//        moreTextualAlignmentCandidateAnswerScore.setScoreWeight(scoreWeight);
//        //5.6、回带文本对齐评分组件
//        CandidateAnswerScore rewindTextualAlignmentCandidateAnswerScore = new RewindTextualAlignmentCandidateAnswerScore();
//        rewindTextualAlignmentCandidateAnswerScore.setScoreWeight(scoreWeight);
//        //5.7、热词评分组件
//        CandidateAnswerScore hotCandidateAnswerScore = new HotCandidateAnswerScore();
//        hotCandidateAnswerScore.setScoreWeight(scoreWeight);
//        //5.8、组合候选答案评分组件
//        CombinationCandidateAnswerScore combinationCandidateAnswerScore = new CombinationCandidateAnswerScore();
//        combinationCandidateAnswerScore.addCandidateAnswerScore(termFrequencyCandidateAnswerScore);
//        combinationCandidateAnswerScore.addCandidateAnswerScore(termDistanceCandidateAnswerScore);
//        combinationCandidateAnswerScore.addCandidateAnswerScore(termDistanceMiniCandidateAnswerScore);
//        combinationCandidateAnswerScore.addCandidateAnswerScore(textualAlignmentCandidateAnswerScore);
//        combinationCandidateAnswerScore.addCandidateAnswerScore(moreTextualAlignmentCandidateAnswerScore);
//        combinationCandidateAnswerScore.addCandidateAnswerScore(rewindTextualAlignmentCandidateAnswerScore);
//        combinationCandidateAnswerScore.addCandidateAnswerScore(hotCandidateAnswerScore);

        QuestionClassifier questionClassifier = new QuestionClassifier();

        MainSystem questionAnsweringSystem = new MainSystem();
        questionAnsweringSystem.setDataSource(dataSource);
        questionAnsweringSystem.setQuestionClassifier(questionClassifier);
        questionAnsweringSystem.setCandidateAnswerSelect(candidateAnswerSelect);
        //questionAnsweringSystem.setCandidateAnswerScore(combinationCandidateAnswerScore);

        questionAnsweringSystem.answerQuestions();//用datasource里的函数获取并回答问题
    }
}
