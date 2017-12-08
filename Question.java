package src;

import org.apdplat.word.segmentation.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ann on 2016/12/6.
 */
public class Question {
    public String question;
    public final List<Evidence> evidences = new ArrayList<>();
    public QuestionType questionType = QuestionType.PERSON_NAME;
    public String expectAnswer;
    public int num;
    //候选的问题类型，对问题进行分类的时候，可能会有多个类型
    //public final Set<QuestionType> candidateQuestionTypes = new HashSet<>();

    public String getText() {
        StringBuilder text = new StringBuilder();
        for (Evidence evidence : evidences) {
            text.append(evidence.title).append(evidence.snippet);
        }
        return text.toString();
    }

    public List<CandidateAnswer> getAllCandidateAnswer() {
        Map<String, Double> map = new HashMap<>();
        for (Evidence evidence : evidences) {
                for (CandidateAnswer candidateAnswer : evidence.getCandidateAnswerCollection()) {
                    if(!question.contains(candidateAnswer.answer)) {
                        Double score = map.get(candidateAnswer.answer);//这里很巧妙，不同evidence得到的不同candidateanswer可能是一样的answer，得分累加
                        Double candidateAnswerFinalScore = candidateAnswer.score + evidence.score; //候选答案的分值和证据的分值 用于计算最终的候选答案分值
                        if (score == null) {
                            score = candidateAnswerFinalScore;
                        } else {
                            score += candidateAnswerFinalScore;
                        }
                        map.put(candidateAnswer.answer, score);
                    }
                }

        }
        //组装候选答案，即从map到CandidateAnswer转换
        List<CandidateAnswer> candidateAnswers = new ArrayList<>();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            String answer = entry.getKey();
            Double score = entry.getValue();
            if (answer != null && score != null && score > 0 && score < Double.MAX_VALUE) {
                CandidateAnswer candidateAnswer = new CandidateAnswer();
                candidateAnswer.answer=answer;
                candidateAnswer.score=score;
                candidateAnswers.add(candidateAnswer);
            }
        }

        Collections.sort(candidateAnswers);
        Collections.reverse(candidateAnswers);

        //分值归一化
        if (candidateAnswers.size() > 0) {
            double baseScore = candidateAnswers.get(0).score;//第一个的score最大
            for (CandidateAnswer candidateAnswer : candidateAnswers) {
                double score = candidateAnswer.score / baseScore;
                candidateAnswer.score=score;
            }
        }

        return candidateAnswers;
    }

    public List<String> getWords() {
        List<String> result = new ArrayList<>();
        List<Word> words = WordParser.parse(question.replace("?", "").replace("？", ""));
        for (Word word : words) {
            result.add(word.getText());
        }
        return result;
    }

    public Map.Entry<String, Integer> getHot() {//得到问题中的热词
        List<String> questionWords = getWords();
        Map<String, Integer> map = new HashMap<>();
        List<Word> words = WordParser.parse(getText());
        for (Word word : words) {
            Integer count = map.get(word.getText());
            if (count == null) {
                count = 1;
            } else {
                count++;
            }
            map.put(word.getText(), count);
        }

        Map<String, Integer> questionMap = new HashMap<>();//过滤掉一个字母长的单词,map变成questionMap
        for (String questionWord : questionWords) {
            Integer count = map.get(questionWord);
            if (questionWord.length() > 1 && count != null) {
                questionMap.put(questionWord, count);
            }
        }

        List<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String, Integer>>(questionMap.entrySet());//按count排序后取count最大的
        Collections.sort(list,new Comparator<Map.Entry <String,Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }

        });

        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

}
