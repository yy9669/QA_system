package src;

/**
 * Created by Ann on 2016/12/6.
 */
public class CandidateAnswer implements Comparable<CandidateAnswer> {
    public String answer;
    public double score = 1.0;

    public int compareTo(CandidateAnswer a) {
        if (this.score < a.score) {
            return -1;
        } else if (this.score > a.score) {
            return 1;
        } else {
            return 0;
        }
    }

}
