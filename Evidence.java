package src;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ann on 2016/12/6.
 */
public class Evidence {
    public String title;
    public String snippet;
    public double score = 0;
    public int source ;
    public List<CandidateAnswer> candidateAnswerCollection= new ArrayList<>();

    public boolean isEmpty() {
        return candidateAnswerCollection.isEmpty();
    }
    public List<CandidateAnswer> getCandidateAnswerCollection() {
        return candidateAnswerCollection;
    }
}
