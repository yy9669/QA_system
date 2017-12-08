package src;

import org.apdplat.word.recognition.PersonName;
import org.apdplat.word.segmentation.PartOfSpeech;
import org.apdplat.word.segmentation.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xiaohan on 2016/12/13.
 */
public class CandidateAnswerSelect {
    public void select(Question question, Evidence evidence) {
        List<CandidateAnswer> candidateAnswerCollection = new ArrayList<>();
        List<Word> words = WordParser.parseWithoutStopWords(evidence.title + "," + evidence.snippet);
        List<Word> questionWords = WordParser.parse(question.question);
        for (Word word : words) {
            //过滤: 答案不能在问题中，去掉
            if (questionWords.contains(word)) continue;

            //筛选:
//             if (word.getText().length() < 2){
//                 //忽略长度小于2的候选答案：
//                 continue;
//             }

            //ns-ns  nt-nt m*-m t*-t nr-nr
            if(word.getPartOfSpeech().getPos().toLowerCase().startsWith(question.questionType.getPos().toLowerCase())){
                //成为候选答案：
                CandidateAnswer answer = new CandidateAnswer();
                answer.answer = word.getText();
                if (!candidateAnswerCollection.contains(answer)) {
                    candidateAnswerCollection.add(answer);
                }
            }
            //处理人名
            else if(question.questionType.getPos().equals("nr") && (word.getPartOfSpeech()== PartOfSpeech.I || word.getPartOfSpeech().getPos().toLowerCase().equals("n"))){
                if(PersonName.is(word.getText())){
                    //成为候选答案：
            	
                    CandidateAnswer answer = new CandidateAnswer();
                    answer.answer = word.getText();
                    if (!candidateAnswerCollection.contains(answer)) {
                        candidateAnswerCollection.add(answer);
                    }
                }
            }
            else if(question.questionType.getPos().equals("unknown") && (word.getPartOfSpeech()== PartOfSpeech.I || word.getPartOfSpeech().getPos().toLowerCase().indexOf("n")==0)){
            	CandidateAnswer answer = new CandidateAnswer();
                answer.answer = word.getText();
                if (!candidateAnswerCollection.contains(answer)) {
                    candidateAnswerCollection.add(answer);
                }
            }
        }
        evidence.candidateAnswerCollection.addAll(candidateAnswerCollection);
    }

    public void TermDistanceMiniCandidateAnswerScore(Question question, Evidence evidence) {

        //1、对问题进行分词
        List<Word> questionWords = WordParser.parse(question.question);
        //2、对证据进行分词
        List<Word> evidenceWords = WordParser.parse(evidence.title + "," + evidence.snippet);
        for (CandidateAnswer candidateAnswer : evidence.candidateAnswerCollection) {
            //3、计算候选答案的词距
            int distance = 0;
            //3.1 计算candidateAnswer的分布
            List<Integer> candidateAnswerOffes = new ArrayList<>();
            for (int i = 0; i < evidenceWords.size(); i++) {
                Word evidenceWord = evidenceWords.get(i);
                if (evidenceWord.getText().equals(candidateAnswer.answer)) {
                    candidateAnswerOffes.add(i);
                }
            }
            for (Word questionTerm : questionWords) {
                //3.2 计算questionTerm的分布
                List<Integer> questionTermOffes = new ArrayList<>();
                for (int i = 0; i < evidenceWords.size(); i++) {
                    Word evidenceWord = evidenceWords.get(i);
                    if (evidenceWord.getText().equals(questionTerm.getText())) {
                        questionTermOffes.add(i);
                    }
                }
                //3.3 计算candidateAnswer和questionTerm的最小词距
                int miniDistance = Integer.MAX_VALUE;
                for (int candidateAnswerOff : candidateAnswerOffes) {
                    for (int questionTermOff : questionTermOffes) {
                        int abs = Math.abs(candidateAnswerOff - questionTermOff);
                        if (miniDistance > abs) {
                            miniDistance = abs;
                        }
                    }
                }
                if (miniDistance != Integer.MAX_VALUE) {
                    distance += miniDistance;
                }
            }
            if(distance != 0.0){
            double score = candidateAnswer.score / distance;
            candidateAnswer.score += score;}
        }
    }

    public void TextualAlignmentCandidateAnswerScore(Question question, Evidence evidence){

        //1、对问题进行分词
        List<Word> questionTerms = WordParser.parse(question.question);
        int questionTermsSize = questionTerms.size();
        //2、获取证据文本
        String evidenceText = evidence.title + evidence.snippet;
        //将每一个候选答案都放到问题的每一个位置，查找在证据中是否有匹配文本
        for (CandidateAnswer candidateAnswer : evidence.candidateAnswerCollection) {
            //3、计算候选答案的文本对齐
            for (int i = 0; i < questionTermsSize; i++) {
                StringBuilder textualAlignment = new StringBuilder();
                for (int j = 0; j < questionTermsSize; j++) {
                    if (i == j) {
                        textualAlignment.append(candidateAnswer.answer);
                    } else {
                        textualAlignment.append(questionTerms.get(j));
                    }
                }
                String textualAlignmentPattern = textualAlignment.toString();
                //4、演化为多个模式，支持模糊匹配
                List<Word> textualAlignmentPatternTerms = WordParser.parse(textualAlignmentPattern);
                List<String> patterns = new ArrayList<>();
                patterns.add(textualAlignmentPattern);
                StringBuilder str = new StringBuilder();
                int len = textualAlignmentPatternTerms.size();
                for (int t = 0; t < len; t++) {
                    str.append(textualAlignmentPatternTerms.get(t).getText());
                    if (t < len - 1) {
                        str.append(".{0,5}");
                    }
                }
                patterns.add(str.toString());
                //5、判断文本是否对齐
                int count = 0;
                int length = 0;
                for (String pattern : patterns) {
                    //LOG.debug("模式："+pattern);
                    Pattern p = Pattern.compile(pattern);
                    Matcher matcher = p.matcher(evidenceText);
                    while (matcher.find()) {
                        String text = matcher.group();
                        count++;
                        length += text.length();
                    }
                }
                //6、打分
                if (count > 0) {
                    double avgLen = (double) length / count;
                    //问题长度questionLen为正因子
                    //匹配长度avgLen为负因子
                    int questionLen = question.question.length();
                    double score = questionLen / avgLen;
                    candidateAnswer.score+=score;
                }
            }
        }
    }
}
