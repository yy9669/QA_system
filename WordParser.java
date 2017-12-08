package src;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.SegmentationAlgorithm;
import org.apdplat.word.segmentation.Word;
import org.apdplat.word.tagging.PartOfSpeechTagging;
import org.apdplat.word.util.WordConfTools;

import java.util.List;

public class WordParser {
	static{
		String conf = "C:\\Users\\Ann\\Desktop\\AnswerRobot\\resource\\dic\\word.local.conf";
		WordConfTools.forceOverride(conf);
	}
	public static List<Word> parseWithoutStopWords(String str){
		List<Word> words = WordSegmenter.seg(str, SegmentationAlgorithm.MaxNgramScore);
		PartOfSpeechTagging.process(words);
		return words;
	}
	
	public static List<Word> parse(String str) {
        List<Word> words = WordSegmenter.segWithStopWords(str, SegmentationAlgorithm.MaxNgramScore);
        PartOfSpeechTagging.process(words);
        return words;
    }
}
