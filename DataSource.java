package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by Ann on 2016/12/12.
 */
public class DataSource {
    public List<Question> questions = new ArrayList<>();
    public DataSource(String files,int mode) {
        Question qa=new Question();
        BufferedReader reader=null;
        if(mode==0) {
            qa.question=files;
            this.questions.add(qa);
        }
        else {
            File file = new File(files);
            try{
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "Unicode");
                reader = new BufferedReader(isr);
                String line = null;
                while ((line = reader.readLine()) != null) {
                    Question qb=new Question();
                    //System.out.println(line);
                    line = line.trim();
                    String[] ss=line.split("\t");
                    line=ss[1];
                    if (line.length() == 0 || line.startsWith("//") || line.startsWith("#")) {
                        continue;
                    }
                    qb.question=line;
                    System.out.println(qb.question);
                    qb.num=Integer.valueOf(ss[0]).intValue();
                    System.out.println(String.valueOf(ss[0]));
                    questions.add(qb);
                }
            }catch (Exception e) {}finally{
                try{
                    reader.close();
                }catch (Exception e) {}
            }
        }
        System.out.println(questions.size());
    }
    public  List<Question>  getAndAnswerQuestions(MainSystem mysystem) {
        File file1=new File("C:\\Users\\Ann\\Desktop\\localoutput.txt");
        File file2=new File("C:\\Users\\Ann\\Desktop\\weboutput.txt");
        List<Question> localevidencedquestions= new ArrayList<>();
        for (Question question :questions) {
            localevidencedquestions.add(evidencefinder.localEvidence(question));
        }
        this.questions=localevidencedquestions;
       mysystem.answerQuestions(questions,file1);

        List<Question> webevidencedquestions= new ArrayList<>();
        for (Question question :questions) {
            webevidencedquestions.add(evidencefinder.webEvidence(question));
        }
        this.questions=webevidencedquestions;
        mysystem.answerQuestions(questions,file2);

        return questions;
    }
}


